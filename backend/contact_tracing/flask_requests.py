import flask
from flask import request, jsonify, Flask
import json
import sqlite3

app = Flask(__name__)

def db_connection():
    conn = None
    try:
        conn = sqlite3.connect("ct.sqlite") 
    except: sqlite3.error as e:
        print(e)
    return conn
:
#gets a new code from the db
@app.route("/newCode")
def newCode():
    conn = db_connection()
    m = SecretHandler()
    return get_new(m)

#checks if person was in contact with an infected person 
@app.route("/check")
def check(codes): 
    conn = db_connection()
    m = ContactTracing()
    if check_compromised(m, codes):
        return True
    else:
        return False

#sends phone's location,current time, and present code to db 
@app.route("/locationSend", methods=["POST"])
def locationSend(lat, lon, tim, code):
    conn = db_connection()
    m = Locations()
    report_location(m, code, lat, lon, tim)

#sends infected persons codes to the db
@app.route("/codeSend", methods=["POST"])
def codeSend(codes):
    conn = db_connection()
    m = SecretHandler()
    mark_compromised(m, codes)

