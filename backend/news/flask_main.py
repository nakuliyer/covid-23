from flask import Flask
from .news import get_news

app = Flask(__name__)


@app.route("/news")
def news_route():
    return get_news()
