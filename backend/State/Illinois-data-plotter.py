import requests 
import matplotlib.pyplot as plt
r = requests.get('https://api.covidtracking.com/v1/states/il/daily.json')
j = r.json()
str1 = ""
str2 = ""
xl = []
yl = []
for d in j:
    for key, value in d.items():
        if key == "date":
            xl.append(value)
        if key == "positive":
            yl.append(value)
        str2 = str(key) + ' : ' + str(value) + '\n'
        str1 += str2 
#print(str1)
plt.plot(xl, yl)
plt.savefig("plot.png")
#plt.show()

