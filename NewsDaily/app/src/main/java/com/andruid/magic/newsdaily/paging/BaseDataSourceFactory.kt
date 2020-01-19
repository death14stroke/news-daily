package com.andruid.magic.newsdaily.paging

import androidx.paging.DataSource
import com.andruid.magic.newsloader.model.NewsOnline

class BaseDataSourceFactory<T : DataSource<Int, NewsOnline>>(val creator: () -> T) :
    DataSource.Factory<Int, NewsOnline>() {
    override fun create(): DataSource<Int, NewsOnline> = creator()
}