from flask import Flask

import sys
import os

app = Flask(__name__)


@app.route('/ping')
def ping_pong():
    return 'pong'


sys.path.append(os.path.abspath(os.path.join(__file__, "../../../")))
import backend.contact_tracing.flask
import backend.news.flask_main

if __name__ == '__main__':
    # Threaded option to enable multiple instances for multiple user access support
    app.run(threaded=True, port=5000)
