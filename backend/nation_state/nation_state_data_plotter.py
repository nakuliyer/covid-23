import requests
import matplotlib.pyplot as plt

urls = ["https://api.covidtracking.com/v1/us/daily.json", "https://api.covidtracking.com/v1/states/il/daily.json"]


def get_nation_state_data(i):
    r = requests.get(urls[i])
    j = r.json()

    times = []
    positives = []
    totals = []
    percentages = []
    j.sort(key=lambda x: x["date"])
    for d in j:
        date_string = str(d["date"])
        times.append(date_string[:4] + "-" + date_string[4:6] + "-" + date_string[6:])
        positives.append(d["positive"])
        totals.append(d["totalTestResults"])
        percentages.append(d["positive"] / d["totalTestResults"])

    return {"times": times, "positives": positives, "totals": totals, "percentages": percentages}


for i, url in enumerate(urls):
    r = requests.get(url)
    j = r.json()

    times = []
    positives = []
    totals = []
    percentages = []
    j.sort(key=lambda x: x["date"])
    for d in j:
        date_string = str(d["date"])
        times.append(date_string[:4] + "-" + date_string[4:6] + "-" + date_string[6:])
        positives.append(d["positive"])
        totals.append(d["totalTestResults"])
        percentages.append(d["positive"] / d["totalTestResults"])

    plt.figure(figsize=(18, 10), dpi=180)
    plt.title("Percentage of tests that were positive")
    plt.plot(times, percentages)
    plt.xticks(times[::20], visible=True, rotation="horizontal")
    if i == 0:
        plt.savefig("backend/plt/nationwide_%.png")
    elif i == 1:
        plt.savefig("backend/plt/illinois_%.png")

    plt.figure(figsize=(18, 10), dpi=180)
    plt.title("Tests and positive cases")
    plt.plot(times, positives, label="Positive cases")
    plt.plot(times, totals, label="Number of people tested")
    plt.legend()
    plt.xticks(times[::20], visible=True, rotation="horizontal")
    if i == 0:
        plt.savefig("backend/plt/nationwide_cases.png")
    elif i == 1:
        plt.savefig("backend/plt/illinois_cases.png")
