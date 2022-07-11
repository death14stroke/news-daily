package com.death14stroke.newsdaily.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.death14stroke.newsdaily.data.model.Result
import com.death14stroke.newsdaily.data.repository.MainRepository
import com.death14stroke.newsloader.data.model.Category
import com.death14stroke.newsloader.data.model.News

/**
 * Paging source for fetching news for a specific [country] and [category]
 */
class NewsPagingSource(
    private val repository: MainRepository,
    private val country: String,
    private val category: Category
) : PagingSource<Int, News>() {
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, News> {
        val page = params.key ?: 0
        val pageSize = params.loadSize

        return when (val result = repository.loadHeadlines(country, category, page, pageSize)) {
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
                LoadResult.Error(result.throwable ?: Throwable(result.error))
            }
            is Result.Loading -> {
                LoadResult.Error(Throwable("Loading"))
            }
        }
    }

    override fun getRefreshKey(state: PagingState<Int, News>): Int? = null
}