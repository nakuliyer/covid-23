import requests

result = requests.get('https://api.covidtracking.com/v1/us/daily.json')
print(result.json()[0]['date'])
for i in range(len(result.json())):
    print("Date: " + str(result.json()[i]['date']))
    print("Positive Cases: " + str(result.json()[i]['positive']))