import sqlite3
from utils import gen_random

DB_LOCATION = "backend/contact_tracing/ct.sqlite"

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
