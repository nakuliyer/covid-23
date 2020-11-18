from flask import Flask

import sys
import os

app = Flask(__name__)


@app.route('/ping')
def ping_pong():
    return 'pong'


sys.path.append(os.path.abspath(os.path.join(__file__, "../../../")))
import backend.contact_tracing.flask
