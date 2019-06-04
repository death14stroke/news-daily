def paginate(size, page, page_size):
    upper_lim = min(page * page_size, size)
    return True if upper_lim < size else False
