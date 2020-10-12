import requests 
r = requests.get('https://api.covidtracking.com/v1/states/current.json')
j = r.json()
str1 = ""
str2 = ""
for d in j:
    for key, value in d.items():
        str2 = str(key) + ' : ' + str(value) + '\n'
        str1 += str2 
print(str1)
