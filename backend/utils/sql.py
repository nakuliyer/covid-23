import sqlite3


class SQLQueryMaker:
    def __init__(self, db):
        self.conx = sqlite3.connect(db)
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
