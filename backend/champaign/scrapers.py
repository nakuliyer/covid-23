# this is an upgraded equivalent of requests that uses chromium internally to load stuff with javascript
from requests_html import HTMLSession
import requests
import json
import re # RegEx!
import itertools

from utils import get_unix_timestamp


class SplunkScraper():
    COOKIES_URL = "https://go.illinois.edu/COVIDTestingData"

    # this url is really difficult to authenticate into
    SID_URL = "https://splunk-public.machinedata.illinois.edu/en-US/splunkd/__raw/servicesNS/splunk-public/uofi_" \
              "shield_public_APP/search/jobs"

    # which is why this is hard coded and periodically updated, which kind of sucks rn
    UNIQUE_ID_1 = "_c3BsdW5rLXB1YmxpYw_c3BsdW5rLXB1YmxpYw_dW9maV9zaGllbGRfcHVibGljX0FQUA__search1_1602716641.412791"
    UNIQUE_ID_2 = "_c3BsdW5rLXB1YmxpYw_c3BsdW5rLXB1YmxpYw_dW9maV9zaGllbGRfcHVibGljX0FQUA__search2_1602719185.413424"
    UNIQUE_ID_3 = "_c3BsdW5rLXB1YmxpYw_c3BsdW5rLXB1YmxpYw_dW9maV9zaGllbGRfcHVibGljX0FQUA__search3_1602715478.412529"
    UNIQUE_ID_4 = "_c3BsdW5rLXB1YmxpYw_c3BsdW5rLXB1YmxpYw_dW9maV9zaGllbGRfcHVibGljX0FQUA__search4_1602715478.412530"

    #
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
        """ TODO: Also load up SID_URL """
        r = self.session_.get(self.COOKIES_URL)
        r.html.render()

        html = requests.get("https://go.illinois.edu/COVIDTestingData", allow_redirects=True)

        # Mad hacking to follow:
        cookies = html.history[-2].headers["Set-Cookie"]
        main_key_name = "splunkd_8000="
        main_key_value = cookies[cookies.find(main_key_name) + len(main_key_name): cookies.find(";", cookies.find(main_key_name))]
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


if __name__ == "__main__":
    scraper = SplunkScraper()
    scraper.create_session()
    scraper.load_up_cookies()
    if scraper.load_up_data():
        scraper.store_data()
