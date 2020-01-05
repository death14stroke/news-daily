package com.andruid.magic.newsdaily.headlines

import androidx.paging.DataSource
import com.andruid.magic.newsloader.model.News

class NewsDataSourceFactory(val country : String, val category : String) : DataSource.Factory<Int, News>() {

    override fun create(): DataSource<Int?, News?> {
        return NewsDataSource(country, category)
    }
}