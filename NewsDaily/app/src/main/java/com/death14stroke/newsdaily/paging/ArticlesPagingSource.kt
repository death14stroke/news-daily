package com.death14stroke.newsdaily.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.death14stroke.newsdaily.data.repository.MainRepository
import com.death14stroke.newsloader.data.model.News
import com.death14stroke.newsloader.data.model.Result

private const val DEFAULT_LANGUAGE = "en"

class ArticlesPagingSource(private val repository: MainRepository, private val query: String) :
    PagingSource<Int, News>() {
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, News> {
        val page = params.key ?: 0
        val pageSize = params.loadSize

        return when (val result =
            repository.loadArticles(DEFAULT_LANGUAGE, query, page, params.loadSize)) {
            is Result.Success -> {
                val (newsList) = result.data
                val hasMore = newsList.size < pageSize
                LoadResult.Page(
                    data = newsList,
                    prevKey = null,
                    nextKey = if (hasMore) page + 1 else null
                )
            }
            is Result.Error -> {
                LoadResult.Error(Throwable(result.error))
            }
            is Result.Loading -> {
                LoadResult.Error(Throwable("Loading"))
            }
        }
    }

    override fun getRefreshKey(state: PagingState<Int, News>): Int? = null
}