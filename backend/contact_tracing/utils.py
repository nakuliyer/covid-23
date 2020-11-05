import random
import string
import math


def gen_random(length=24):
    return ''.join(random.choice(string.ascii_uppercase + string.digits) for _ in range(length))


# https://stackoverflow.com/questions/639695/how-to-convert-latitude-or-longitude-to-meters
def convert_lat_long_to_m(lat1, long1, lat2, long2):
    radius_earth = 6378.137  # in km
    lat_delta = lat2 * math.pi / 180 - lat1 * math.pi / 180
    long_delta = long2 * math.pi / 180 - long1 * math.pi / 180
    a = math.sin(lat_delta / 2) * math.sin(lat_delta / 2) + \
        math.cos(lat1 * math.pi / 180) * math.cos(lat2 * math.pi / 180) \
        * math.sin(long_delta / 2) * math.sin(long_delta / 2)
    c = 2 * math.atan2(math.sqrt(a), math.sqrt(1 - a))
    d = radius_earth * c
    return d * 1000  # meters


def sqlize_list(l):
    """ magic """
    return ', '.join(map(lambda x: "(\"{}\")".format(x), l))
