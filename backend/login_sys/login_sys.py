### I JUST COPIED SOME OF MY PREVIOUS STUFF THAT MIGHT BE HELPFUL HERE

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