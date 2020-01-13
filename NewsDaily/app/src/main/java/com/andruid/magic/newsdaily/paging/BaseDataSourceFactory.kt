package com.andruid.magic.newsdaily.paging

import androidx.paging.DataSource
import com.andruid.magic.newsloader.model.News

class BaseDataSourceFactory<T : DataSource<Int, News>>(val creator: () -> T) :
    DataSource.Factory<Int, News>() {
    override fun create(): DataSource<Int, News> = creator()
}