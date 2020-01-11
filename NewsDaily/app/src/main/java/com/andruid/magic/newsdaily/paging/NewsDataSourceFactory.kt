package com.andruid.magic.newsdaily.paging

import androidx.paging.DataSource
import com.andruid.magic.newsloader.model.News
import kotlinx.coroutines.CoroutineScope

class NewsDataSourceFactory(private val scope: CoroutineScope,
                            private val country: String,
                            private val category: String
) : DataSource.Factory<Int, News>() {
    override fun create(): DataSource<Int, News> {
        return NewsDataSource(scope, country, category)
    }
}