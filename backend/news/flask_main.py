from app import app
from .news import get_news


@app.route("/news")
def news_route():
    return get_news()
