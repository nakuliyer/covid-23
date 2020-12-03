import sys
import os
import time

ROOT = os.path.abspath(os.path.join(__file__, "../../../"))
if __name__ == "__main__":
    sys.path.append(ROOT)
from backend.utils.sql import SQLQueryMaker
DB_LOCATION = os.path.join(ROOT, "backend/contact_tracing/ct.sqlite")


class Locations:
    """ This is what the front-end pings to update the location """
    def __init__(self):
        self.sql = SQLQueryMaker(DB_LOCATION)

        if not self.sql.table_exists("locations"):
            self.sql.execute(""" CREATE TABLE locations (code varchar(255), timestamp timestamp, """ +
                             """ lat decimal(20, 15), long decimal(20, 15)) """)

    def report_location(self, current_code, lat, long, timestamp=None):
        """ Adds the user's current code to the db with their timestamp, latitude, and longitude """
        if timestamp is None:
            timestamp = int(time.time()) * 1000
        self.sql.execute(""" INSERT INTO locations (code, timestamp, lat, long) VALUES ("{}", {}, {}, {})"""
                         .format(current_code, timestamp, lat, long))

    def routine_delete(self, max_period=1.21e+9):
        """ Deletes all records before 14 days (or `max_period` milliseconds) """
        before_date = int(time.time()) * 1000 - max_period
        self.sql.execute(""" DELETE FROM locations WHERE timestamp < {} """.format(before_date))
