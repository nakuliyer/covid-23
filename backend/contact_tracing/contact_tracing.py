import sys
import os
import time

ROOT = os.path.abspath(os.path.join(__file__, "../../../"))
if __name__ == "__main__":
    sys.path.append(ROOT)
from backend.contact_tracing.utils import convert_lat_long_to_m, sqlize_list
from backend.utils.sql import SQLQueryMaker

DB_LOCATION = os.path.join(ROOT, "backend/contact_tracing/ct.sqlite")


class ContactTracing:
    def __init__(self):
        self.sql = SQLQueryMaker(DB_LOCATION)

        if not self.sql.table_exists("ct_main"):
            self.sql.execute(""" CREATE TABLE ct_main (first_secret varchar(255), second_secret varchar(255),""" +
                             """ timestamp timestamp, distance decimal(20, 15)) """)

    def create_contacts(self, max_distance=7, max_time=10000):
        """ Goes through the locations table to find contacts within `max_distance` meters and `max_time` seconds and
        adds to ct table """
        all_locations = self.sql.execute(""" SELECT * FROM locations """).fetchall()
        if len(all_locations) < 2:
            return

        for first_index in range(0, len(all_locations) - 1, 1):
            for second_index in range(first_index + 1, len(all_locations), 1):
                (code1, ts1, lat1, long1) = all_locations[first_index]
                (code2, ts2, lat2, long2) = all_locations[second_index]

                d = convert_lat_long_to_m(lat1, long1, lat2, long2)
                if d <= max_distance and abs(ts1 - ts2) <= max_time:
                    self.sql.execute(""" INSERT INTO ct_main (first_secret, second_secret, timestamp, distance)""" +
                                     """ VALUES ("{}", "{}", {}, {}) """.format(code1, code2, ts1, d))

        self.sql.execute(""" DELETE FROM locations """)

    def routine_delete(self, max_period=1.21e+9):
        """ Deletes all records before 14 days (or `max_period` milliseconds) """
        before_date = int(time.time()) * 1000 - max_period
        self.sql.execute(""" DELETE FROM locations WHERE timestamp < {} """.format(before_date))

    def check_compromised(self, my_codes):
        """ Checks if someone with the given `my_codes` has been near a compromised code """
        sql_my_codes = sqlize_list(my_codes)
        interactions = self.sql.execute(""" SELECT * FROM ct_main WHERE (first_secret in {} or second_secret in {})"""
                                        .format(sql_my_codes, sql_my_codes)).fetchall()
        other_codes = []
        for (first_secret, second_secret, timestamp, distance) in interactions:
            other_codes.append(first_secret if second_secret in sql_my_codes else second_secret)  # the other person
        if len(other_codes) == 0:
            return {"result": False}
        sql_other_codes = ' or '.join(map(lambda x: "code==\"{}\"".format(x), other_codes))
        result = self.sql.exists(""" SELECT COUNT(*) FROM compromised_codes WHERE ({})""".format(sql_other_codes))
        return {"result": result}