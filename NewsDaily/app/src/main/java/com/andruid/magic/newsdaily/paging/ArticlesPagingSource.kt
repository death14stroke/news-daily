package com.andruid.magic.newsdaily.paging

import androidx.paging.PagingSource
import com.andruid.magic.newsdaily.database.entity.NewsItem
import com.andruid.magic.newsdaily.database.entity.toNewsItem
import com.andruid.magic.newsloader.data.api.NewsRepository
import com.andruid.magic.newsloader.data.model.Result

class ArticlesPagingSource(private val query: String) : PagingSource<Int, NewsItem>() {
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, NewsItem> {
        val page = params.key ?: 1
        val result = NewsRepository.loadArticles("en", query, page, params.loadSize)

        return if (result.status == Result.Status.SUCCESS && result.data != null) {
            val (newsList, loadMore) = result.data!!
            LoadResult.Page(
                data = newsList.map { news -> news.toNewsItem("search") },
                prevKey = null,
                nextKey = if (loadMore) page + 1 else null
            )
        } else {
            LoadResult.Error(Throwable(result.message))
        }
    }
}