def main(coordinate):
    from geopy.geocoders import Nominatim
    import requests
    import us
    import datetime
    import numpy as np
    import numpy.linalg as la
    from java import (jarray, jfloat)
    geolocator = Nominatim(user_agent="script")
    location = geolocator.reverse(coordinate)
    state_names = ["Alaska", "Alabama", "Arkansas", "American Samoa", "Arizona", "California", "Colorado", "Connecticut", "District ", "of Columbia", "Delaware", "Florida", "Georgia", "Guam", "Hawaii", "Iowa", "Idaho", "Illinois", "Indiana", "Kansas", "Kentucky", "Louisiana", "Massachusetts", "Maryland", "Maine", "Michigan", "Minnesota", "Missouri", "Mississippi", "Montana", "North Carolina", "North Dakota", "Nebraska", "New Hampshire", "New Jersey", "New Mexico", "Nevada", "New York", "Ohio", "Oklahoma", "Oregon", "Pennsylvania", "Puerto Rico", "Rhode Island", "South Carolina", "South Dakota", "Tennessee", "Texas", "Utah", "Virginia", "Virgin Islands", "Vermont", "Washington", "Wisconsin", "West Virginia", "Wyoming"]
    location = str(location).split(", ")
    index = 0
    for word in location:
        if word in state_names:
            index = location.index(word)
    state = location[index]
    state = us.states.lookup(state).abbr.lower()
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
        cases.insert(0, result.json()[i]['positive'])
    x = np.array([np.ones(len(dates)), dates])
    x = np.transpose(x)
    y = np.array([cases])
    y = np.transpose(y)

    A_t_A = np.transpose(x) @ x
    A_t_y = np.transpose(x) @ y
    beta = la.solve(A_t_A, A_t_y)
    values = (jarray)(jfloat)([beta[0][0], beta[1][0]])
    return values
