import sqlite3
from utils import gen_random

DB_LOCATION = "backend/contact_tracing/ct.sqlite"


# Overall Idea for Contact Tracing:
# The mobile will can call the `get_new()` in ContactTracingSQL to get a new
# code that'll then be dumped into the `used_codes` table. This should happen
# multiple times a day locally. The mobile app should locally store ALL the codes
# that the person has seen (ideally forever, but for minimizing storage this might be
# deleted every 14 days or whatever).
#
# When two phones are in contact with each other (i.e. 6 feet), we create an
# `AnonymousContact` this holds `first_secret` (one mobile's most recent code) and
# `second_secret` (the other mobile's most recent code). A `time_stamp` is recorded
# and the actual distance may or may not be recorded. We add this entry to the
# ct_main database.
#
# So when one person gets the virus, ALL the codes from their local phone are added to
# to the `compromised_codes` database.
#
# And then the other mobile phones regularly check `ct_main` to check whether they've
# linked one of the compromised codes. It can be extended so that we check for friends
# of friends, etc.
#
# Yay


class AnonymousContact:
    def __init__(self, first_secret, second_secret, time_stamp, distance):
        self.first_secret = first_secret
        self.second_secret = second_secret
        self.time_stamp = time_stamp
        self.distance = distance


class ContactTracingSQL:
    def __init__(self):
        self.sql = SQLQueryMaker()

        if not self.sql.table_exists("used_codes"):
            self.sql.execute(""" CREATE TABLE used_codes (code varchar(255)) """)

    def get_new(self):
        r = gen_random()
        while self.sql.exists(""" SELECT code FROM used_codes WHERE code == "{}" """.format(r)):
            r = gen_random()
        self.sql.execute(""" INSERT INTO used_codes (code) VALUES ("{}") """.format(r))
        return r

# The AnonymousPersonSQL just holds usernames and passwords and is basically
# # unrelated to the rest of this / can be mved in future.
class AnonymousPersonSQL:
    def __init__(self):
        self.sql = SQLQueryMaker()

        if not self.sql.table_exists("users"):
            self.sql.execute(""" CREATE TABLE users (name varchar(255), password varchar(255)) """)

    def create_person(self, name, password):
        if not self.sql.exists(""" SELECT COUNT(name) FROM users WHERE name == "{}" """.format(name)):
            self.sql.execute(""" INSERT INTO users (name, password) VALUES ("{}", "{}") """.format(name, password))


class SQLQueryMaker:
    def __init__(self):
        self.conx = sqlite3.connect(DB_LOCATION)
        self.cur = self.conx.cursor()

    def execute(self, sql):
        res = self.cur.execute(sql)
        self.conx.commit()
        return res

    def exists(self, sql):
        try:
            return self.execute(sql).fetchone()[0] == 1
        except TypeError:
            return False

    def table_exists(self, table_name):
        return self.exists("SELECT COUNT(*) FROM sqlite_master WHERE name == \"{}\";".format(table_name))

    def close(self):
        self.conx.commit()
        self.conx.close()


ct = ContactTracingSQL()
print(ct.get_new())
print(ct.get_new())

users = AnonymousPersonSQL()

# sqm = SQLQueryMaker()
# sqm.create_contact_tracer_table()
# sqm.close()
