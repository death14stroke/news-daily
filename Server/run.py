import simplejson
from flask import Flask, request
from news import get_headlines

app = Flask(__name__)


@app.route('/headlines', methods=['GET'])
def headlines():
    page = int(request.args.get('page'))
    page_size = int(request.args.get('page_size'))
    country = request.args.get('country')
    category = request.args.get('category')
    resp = simplejson.dumps(get_headlines(country=country, page=page, page_size=page_size, category=category))
    return resp


if __name__ == "__main__":
    app.run(host='0.0.0.0', debug=True)
