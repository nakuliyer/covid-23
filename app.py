from flask import Flask, request
from backend.news.news import get_news
from backend.contact_tracing.secret_handler import SecretHandler
from backend.contact_tracing.contact_tracing import ContactTracing
from backend.contact_tracing.locations import Locations
from backend.champaign.scrapers import get_latest_data, get_old_data
from backend.nation_state import get_nation_state_data
from backend.linreg import main_regression

app = Flask(__name__)


@app.route('/ping')
def ping_pong():
    return 'pong'


@app.route("/news")
def news_route():
    return get_news()


@app.route("/get_new_code")
def get_new_code():
    """ gets a new code from the db """
    m = SecretHandler()
    return m.get_new()


@app.route("/check_compromised", methods=["POST"])
def check_compromised():
    """ checks if person was in contact with an infected person """
    m = ContactTracing()
    return m.check_compromised(request.json["codes"])


@app.route("/post_location", methods=["POST"])
def post_location():
    """ sends phone's location,current time, and present code to db  """
    m = Locations()
    if "time" in request.json:
        m.report_location(request.json["code"], request.json["lat"], request.json["long"], request.json["time"])
    else:
        m.report_location(request.json["code"], request.json["lat"], request.json["long"])
    return {"success": True}


@app.route("/post_compromised_codes", methods=["POST"])
def post_compromised_code():
    """ sends infected persons codes to the db """
    m = SecretHandler()
    m.mark_compromised(request.json["codes"])
    return {"success": True}


@app.route("/am_i_compromised", methods=["POST"])
def post_am_i_compromised():
    m = SecretHandler()
    return {"result": m.am_i_compromised(request.json["codes"])}


@app.route("/mark_recovered", methods=["POST"])
def post_mark_recovered():
    """ sends infected persons codes to the db """
    m = SecretHandler()
    m.mark_not_compromised(request.json["codes"])
    return {"success": True}


@app.route("/create_contacts", methods=["POST"])
def create_contacts(max_distance=7, max_time=10000):
    """ goes through locations, creates contacts, and deletes locations table """
    m = ContactTracing()
    m.create_contacts(max_distance, max_time)
    return {"success": True}


@app.route("/routine_delete_all", methods=["DELETE"])
def routine_delete_all(max_period=1.21e+9):
    m = ContactTracing()
    m.routine_delete(max_period)
    m = Locations()
    m.routine_delete(max_period)
    return {"success": True}


@app.route("/champaign")
def get_champaign():
    return get_latest_data()


@app.route("/champaign_temp_fix")
def get_champaign_temp_fix():
    return get_old_data("backend/champaign/local_save/data_1606118897993.dat")


@app.route("/nation")
def get_nation():
    return get_nation_state_data(0)


@app.route("/illinois")
def get_illinois():
    return get_nation_state_data(1)


@app.route("/regression", methods=["POST"])
def get_regression():
    return {"result": main_regression(request.json["state"])}


if __name__ == '__main__':
    # Threaded option to enable multiple instances for multiple user access support
    app.run(threaded=True, port=5000)
