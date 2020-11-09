import time


def get_unix_timestamp():
    """ Gets unix timestamp in milliseconds """
    return int(time.time() * 1000)
