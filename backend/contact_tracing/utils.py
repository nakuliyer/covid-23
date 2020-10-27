import random
import string


def gen_random(length=24):
    return ''.join(random.choice(string.ascii_uppercase + string.digits) for _ in range(length))