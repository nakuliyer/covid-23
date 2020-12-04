import sys
import os
import time

ROOT = os.path.abspath(os.path.join(__file__, "../../../"))
if __name__ == "__main__":
    sys.path.append(ROOT)
from backend.contact_tracing.utils import gen_random, sqlize_list
from backend.utils.sql import SQLQueryMaker
DB_LOCATION = os.path.join(ROOT, "backend/contact_tracing/ct.sqlite")


class SecretHandler:
    """ Handles the used codes, marking codes as compromised, etc """

    def __init__(self):
        self.sql = SQLQueryMaker(DB_LOCATION)

        if not self.sql.table_exists("used_codes"):
            self.sql.execute(""" CREATE TABLE used_codes (code varchar(255)) """)
        if not self.sql.table_exists("compromised_codes"):
            self.sql.execute(""" CREATE TABLE compromised_codes (code varchar(255)) """)

    def get_new(self):
        """ Returns a new code and adds it to used codes """
        r = gen_random()
        while self.sql.exists(""" SELECT code FROM used_codes WHERE code == "{}" """.format(r)):
            r = gen_random()
        self.sql.execute(""" INSERT INTO used_codes (code) VALUES ("{}") """.format(r))
        return r

    def mark_compromised(self, codes):
        """ Adds codes to compromised """
        values = sqlize_list(codes)
        self.sql.execute(""" INSERT INTO compromised_codes (code) VALUES {} """.format(values))

    def am_i_compromised(self, my_codes):
        sql_my_codes = sqlize_list(my_codes)
        result = self.sql.exists(""" SELECT COUNT(*) FROM compromised_codes WHERE (code in {})""".format(sql_my_codes))
        return result

    def mark_not_compromised(self, codes):
        sql_my_codes = sqlize_list(codes)
        self.sql.execute(""" DELETE FROM compromised_codes WHERE (code in {}) """.format(sql_my_codes))
