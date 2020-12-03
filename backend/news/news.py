import requests
import json
from datetime import date, time, datetime

import os

ROOT = os.path.abspath(os.path.join(__file__, "../../../"))
JSON_SAVE = os.path.join(ROOT, "backend/news/save.json")
RAPID_API_URL = "https://coronavirus-smartable.p.rapidapi.com/news/v1/{}/"
# LOCATIONS = ["US", "US-AL", "US-AK", "US-AZ", "US-AR", "US-CA", "US-CO", "US-CT", "US-DE", "US-FL", "US-GA",
#              "US-ID", "US-IL",c "US-IN", "US-IA", "US-KS", "US-KY", "US-LA", "US-ME", "US-MD", "US-MA", "US-MI", "US-MN",
#              "US-MS", "US-MO", "US-MT", "US-NE", "US-NV", "US-NH", "US-NJ", "US-NM", "US-NY", "US-NC", "US-ND", "US-OH",
#              "US-OK", "US-OR", "US-PA", "US-RI", "US-SC", "US-SD", "US-TN", "US-TX", "US-UT", "US-VT", "US-VA", "US-WA",
#              "US-WV", "US-WI", "US-WY"]
LOCATIONS = ["US"]


def get_news():
    try:
        save_file = json.load(open(JSON_SAVE, "r+"))
        if save_file["date"] == str(date.today()):
            return {"use_cached": True, "result": save_file}
    except (json.decoder.JSONDecodeError, KeyError, FileNotFoundError):
        pass  # continue on

    headers = {
        'x-rapidapi-key': "926576883emsh0929c234d91e5d8p19a217jsn1b07e87e770d",
        'x-rapidapi-host': "coronavirus-smartable.p.rapidapi.com"
    }

    state = {"date": str(date.today()), "locations": dict()}
    for location in LOCATIONS:
        url = RAPID_API_URL.format(location)
        response = requests.get(url, headers=headers)
        state["locations"][location] = json.loads(response.text)
    json.dump(state, open(JSON_SAVE, "w+"))
    return {"use_cached": False, "result": state}


if __name__ == "__main__":
    get_news()
