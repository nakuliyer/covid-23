import requests
from datetime import date, time, datetime

statestore = {}
apicalldates = {}
def News_get(state):

    if state in apicalldates:

        if apicalldates.get(state) == date.today():
            response = statesstore.get(state)   
            return response


    url = "https://coronavirus-smartable.p.rapidapi.com/news/v1/" +  US + "/"

    headers = {
            'x-rapidapi-key': "926576883emsh0929c234d91e5d8p19a217jsn1b07e87e770d",
                'x-rapidapi-host': "coronavirus-smartable.p.rapidapi.com"
                    }

    response = requests.request("GET", url, headers=headers)
            
            
    statestore.update(state, response.text)
    apicalldates.update(state, date.today())

    return response.text
