import sqlite3


class AnonymousContact:
    def __init__(self, first_secret, second_secret, time_stamp, distance):
        self.first_secret = first_secret
        self.second_secret = second_secret
        self.time_stamp = time_stamp
        self.distance = distance


class SQLQueryMaker:
    def __init__(self):
        self.conx = sqlite3.connect('ct.sqlite')
        self.cur = self.conx.cursor()

    def execute(self, sql):
        """ pretty useless method to simplify things """
        return self.cur.execute(sql)

    def create_contact_tracer_table(self):
        sql = """ SELECT COUNT(*) FROM sqlite_master WHERE name == "ct_main"; """
        exists = self.execute(sql).fetchone()[0] == 1
        if not exists:
            sql = """ CREATE TABLE ct_main 
                     (first_secret varchar(255), second_secret varchar(255), time_stamp int, distance real) """
            self.execute(sql)

    def close(self):
        self.conx.commit()
        self.conx.close()


sqm = SQLQueryMaker()
sqm.create_contact_tracer_table()
sqm.close()
