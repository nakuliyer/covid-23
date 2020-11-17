import requests
import json
from datetime import date, time, datetime

JSON_SAVE = "backend/news/save.json"
RAPID_API_URL = "https://coronavirus-smartable.p.rapidapi.com/news/v1/{}/"
LOCATIONS = ["US", "US-IL", "US-MA", "US-CA"]


def get_news():
    try:
        save_file = json.load(open(JSON_SAVE, "r+"))
        if save_file["date"] == str(date.today()):
            return save_file
    except (json.decoder.JSONDecodeError, KeyError):
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
    return state


if __name__ == "__main__":
    get_news()
