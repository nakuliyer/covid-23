import requests 
import matplotlib.pyplot as plt
r = requests.get('https://api.covidtracking.com/v1/states/il/daily.json')
j = r.json()

xl = []
yl = []
totals = []
j.sort(key=lambda x: x["date"])
for d in j:
    for key, value in d.items():
        if key == "date":
            xl.append(str(value)[:4] + "-" + str(value)[4:6] + "-" + str(value)[6:])
        elif key == "positive":
            yl.append(value)
        elif key == "totalTestResults":
            totals.append(value)

plt.figure(figsize=(18, 10), dpi=180)
plt.title("Tests and positive cases")
plt.plot(xl, yl, label="Positive cases")
plt.plot(xl, totals, label="Number of people tested")
plt.legend()
plt.xticks(xl[::20], visible=True, rotation="horizontal")
plt.savefig("backend/plt/illinois_cases.png")
