package com.andruid.magic.newsdaily.articles

import androidx.paging.DataSource
import com.andruid.magic.newsloader.model.News

class ArticlesDataSourceFactory(val language : String, val query : String) : DataSource.Factory<Int, News>() {

    override fun create(): DataSource<Int, News> {
        return ArticlesDataSource(language, query)
    }
}