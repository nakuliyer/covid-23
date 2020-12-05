import requests
import datetime
import numpy as np
import numpy.linalg as la


def main_regression(state):
    url = 'https://api.covidtracking.com/v1/states/' + state + '/daily.json'
    result = requests.get(url)
    cases = []
    dates = []
    for i in range(len(result.json())):
        date = str(result.json()[i]['date'])
        year = date[0:4]
        month = date[4:6]
        day = date[6:]
        date = datetime.datetime(int(year), int(month), int(day)).timetuple().tm_yday
        dates.insert(0, date - 64)
        if result.json()[i]['positive'] is None:
            cases.insert(0, 0)
        else:
            cases.insert(0, result.json()[i]['positive'])
    x = np.array([np.ones(len(dates)), dates])
    x = np.transpose(x)
    y = np.array([cases])
    y = np.transpose(y)

    A_t_A = np.transpose(x) @ x
    A_t_y = np.transpose(x) @ y
    beta = la.solve(A_t_A, A_t_y)
    values = [beta[0][0], beta[1][0]]
    return values
