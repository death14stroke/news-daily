from newsapi import NewsApiClient
from page_resp import paginate
from time import mktime, strptime

API_KEY = 'd96ab6f6d0d44b3ca0eda01c6711da42'
newsapi = NewsApiClient(api_key=API_KEY)


def get_headlines(country, category, page, page_size):
    raw_news = newsapi.get_top_headlines(page_size=page_size, page=page, country=country, category=category)
    status = raw_news['status']
    size = raw_news['totalResults']
    has_more = paginate(size=size, page_size=page_size, page=page)
    articles = []
    if status == 'ok':
        for a in raw_news['articles']:
            obj = {
                'sourceName': a['source']['name'],
                'author': a['author'],
                'title': a['title'],
                'desc': a['description'],
                'url': a['url'],
                'imageUrl': a['urlToImage'],
                'published': int(mktime(strptime(a['publishedAt'], "%Y-%m-%dT%H:%M:%SZ")))*1000,
                'content': a['content']
            }
            articles.append(obj)
    resp = {
        'hasMore': has_more,
        "news": articles
    }
    return resp
