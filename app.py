from flask import Flask
from backend.news.news import get_news
from backend.contact_tracing.secret_handler import SecretHandler
from backend.contact_tracing.contact_tracing import ContactTracing
from backend.contact_tracing.locations import Locations

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


@app.route("/check_compromised")
def check_compromised(codes):
    """ checks if person was in contact with an infected person """
    m = ContactTracing()
    return m.check_compromised(codes)


@app.route("/post_location", methods=["POST"])
def post_location(code, lat, lon, tim):
    """ sends phone's location,current time, and present code to db  """
    m = Locations()
    m.report_location(code, lat, lon, tim)


@app.route("/post_compromised_codes", methods=["POST"])
def post_compromised_code(codes):
    """ sends infected persons codes to the db """
    m = SecretHandler()
    m.mark_compromised(codes)


@app.route("/create_contacts", methods=["POST"])
def create_contacts(max_distance=7, max_time=10000):
    """ goes through locations, creates contacts, and deletes locations table """
    m = ContactTracing()
    return m.create_contacts(max_distance, max_time)


@app.route("/route_delete_all", methods=["DELETE"])
def routine_delete_all(max_period=1.21e+9):
    m = ContactTracing()
    m.routine_delete(max_period)
    m = Locations()
    m.routine_delete(max_period)


if __name__ == '__main__':
    # Threaded option to enable multiple instances for multiple user access support
    app.run(threaded=True, port=5000)
