# this is an upgraded equivalent of requests that uses chromium internally to load stuff with javascript
from threading import Thread

from requests_html import HTMLSession
import requests
import json
import re  # regex
import sys
import numpy as np
import datetime
import matplotlib.pyplot as plt
sys.path.append('../../..')

from .utils import get_unix_timestamp


class SplunkScraper:
    COOKIES_URL = "https://go.illinois.edu/COVIDTestingData"
    SID_URL = "https://splunk-public.machinedata.illinois.edu/en-US/splunkd/__raw/servicesNS/splunk-public/uofi_" \
              "shield_public_APP/search/jobs"
    BASE_SEARCHES_URL = "https://splunk-public.machinedata.illinois.edu/en-US/splunkd/__raw/services/search/jobs/"
    QUERY_PARAMS = "/results_preview?output_mode=json_cols&count={}&offset=0&show_metadata=true&_={}"

    RAW_SAVE_PATH = "backend/champaign/local_save"

    def __init__(self):
        self.session_ = None
        self.data_ = None
        self.unique_id_1_ = None
        self.unique_id_2_ = None
        self.unique_id_3_ = None
        self.unique_id_4_ = None

    def create_session(self):
        self.session_ = HTMLSession()

    def load_up_cookies(self):
        r = self.session_.get(self.COOKIES_URL)
        r.html.arender()

        html = requests.get("https://go.illinois.edu/COVIDTestingData", allow_redirects=True)

        # Mad hacking to follow:
        cookies = html.history[-2].headers["Set-Cookie"]
        main_key_name = "splunkd_8000="
        main_key_value = cookies[cookies.find(main_key_name) + len(main_key_name): cookies.find(";", cookies.find(
            main_key_name))]
        token_name = "splunkweb_csrf_token_8000="
        token_value = cookies[cookies.find(token_name) + len(token_name): cookies.find(";", cookies.find(token_name))]
        cookies = main_key_name + main_key_value + "; " + token_name + token_value + "; " + "token_key=" + token_value
        r = requests.post(self.SID_URL, allow_redirects=True, headers={
            'User-Agent': 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/39.0.2171.95 Safari/537.36',
            "Accept": "text/javascript, text/html, application/xml, text/xml, */*",
            "Accept-Encoding": "gzip, deflate, br",
            "Cookie": cookies,
            "X-Requested-With": "XMLHttpRequest",
            "X-Splunk-Form-Key": token_value
        })

        latest_unique_jobs = list(re.finditer(r'"sid">.+<', r.text))[:4]

        self.unique_id_1_ = latest_unique_jobs[0].group()[6:-1]
        self.unique_id_2_ = latest_unique_jobs[1].group()[6:-1]
        self.unique_id_3_ = latest_unique_jobs[2].group()[6:-1]
        self.unique_id_4_ = latest_unique_jobs[3].group()[6:-1]

    def load_up_data(self):
        """ Raw data. """
        self.data_ = dict()
        b = True

        url_1 = self.BASE_SEARCHES_URL + self.unique_id_1_ + self.QUERY_PARAMS.format(1000, get_unix_timestamp())
        text_1 = json.loads(self.session_.get(url_1).text)
        try:
            self.data_["total_cases"] = text_1["columns"][0][0]
        except KeyError:
            self.data_["total_cases"] = []
            print("Failed on total cases")
            b = False

        url_2 = self.BASE_SEARCHES_URL + self.unique_id_2_ + self.QUERY_PARAMS.format(1000, get_unix_timestamp())
        text_2 = json.loads(self.session_.get(url_2).text)
        try:
            self.data_["7_day_positivity_avg"] = text_2["columns"]
        except KeyError:
            self.data_["7_day_positivity_avg"] = []
            print("Failed on 7 day positivity avg")
            b = False

        url_3 = self.BASE_SEARCHES_URL + self.unique_id_3_ + self.QUERY_PARAMS.format(10000, get_unix_timestamp())
        text_3 = json.loads(self.session_.get(url_3).text)
        # this data is split into two cases
        try:
            self.data_["new_cases"] = [text_3["columns"][0], text_3["columns"][1]]
            self.data_["case_positivity_percent"] = [text_3["columns"][0], text_3["columns"][2]]
        except KeyError:
            self.data_["new_cases"] = []
            self.data_["case_positivity_percent"] = []
            print("Failed on new cases")
            b = False

        url_4 = self.BASE_SEARCHES_URL + self.unique_id_4_ + self.QUERY_PARAMS.format(10000, get_unix_timestamp())
        text_4 = json.loads(self.session_.get(url_4).text)
        try:
            self.data_["total_daily_tests_results"] = text_4["columns"]
        except KeyError:
            self.data_["total_daily_tests_results"] = []
            print("Failed on total daily tests results")
            b = False

        return b

    def get_data(self):
        """ Idk if getters/setters is pythonic but eh """
        return self.data_

    def store_data(self):
        f = open("{}/data_{}.dat".format(self.RAW_SAVE_PATH, get_unix_timestamp()), "w+")
        json.dump(self.data_, f, indent=2)
        f.close()

    def read_stored_data(self, path):
        f = open(path, "r")
        self.data_ = json.load(f)
        f.close()

    def plot_data_cases(self, path):
        """ Returns plot-able data in the form of [[date-times], [percentages], [positive cases], [total trials]] """
        self.read_stored_data(path)

        def fmt_date(date_string):
            d = datetime.datetime.strptime(date_string, '%Y-%m-%dT%H:%M:%S.000-05:00')
            return d.strftime("%Y-%m-%d")

        times = list(map(fmt_date, self.data_["new_cases"][0]))
        positives = np.array(list(map(int, self.data_["new_cases"][1])))
        totals = np.array(list(map(int, self.data_["total_daily_tests_results"][1])))
        percentages = positives / totals

        plt.figure(figsize=(18, 10), dpi=180)
        plt.title("Percentage of tests that were positive")
        plt.plot(times, percentages)
        plt.xticks(times[::10], visible=True, rotation="horizontal")
        plt.savefig("backend/plt/champaign_%")
        plt.figure(figsize=(18, 10), dpi=180)
        plt.title("Tests and positive cases")
        plt.plot(times, positives, label="Positive cases")
        plt.plot(times, totals, label="Number of people tested")
        plt.legend()
        plt.xticks(times[::10], visible=True, rotation="horizontal")
        plt.savefig("backend/plt/champaign_cases")


def get_latest_data():
    scraper = SplunkScraper()
    scraper.create_session()
    scraper.load_up_cookies()
    if scraper.load_up_data():
        scraper.store_data()
        return scraper.get_data()
    else:
        print("No data found")


def get_old_data(f_name):
    scraper = SplunkScraper()
    scraper.read_stored_data(f_name)
    return scraper.get_data()


def plot_data_cases(f_name):
    scraper = SplunkScraper()
    scraper.plot_data_cases(f_name)


if __name__ == "__main__":
    print(get_latest_data())
    # plot_data_cases("backend/champaign/local_save/data_1603327183609.dat")