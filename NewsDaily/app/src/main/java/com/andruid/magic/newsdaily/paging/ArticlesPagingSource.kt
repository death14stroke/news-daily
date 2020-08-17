package com.andruid.magic.newsdaily.paging

import androidx.paging.PagingSource
import com.andruid.magic.newsdaily.database.entity.NewsItem
import com.andruid.magic.newsdaily.database.entity.toNewsItem
import com.andruid.magic.newsloader.api.NewsRepository
import com.andruid.magic.newsloader.data.model.ApiResponse
import com.andruid.magic.newsloader.data.model.Result

class ArticlesPagingSource(private val query: String) : PagingSource<Int, NewsItem>() {
    @Suppress("MoveVariableDeclarationIntoWhen")
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, NewsItem> {
        val page = params.key ?: 1
        val result = NewsRepository.loadArticles("en", query, page, params.loadSize)

        return when (result) {
            is Result.Success<ApiResponse> -> {
                return result.data?.let {
                    val (newsList, loadMore) = it
                    LoadResult.Page(
                        data = newsList.map { news -> news.toNewsItem() },
                        prevKey = null,
                        nextKey = if (loadMore) page + 1 else null
                    )
                } ?: LoadResult.Error(Throwable("No results found"))
            }
            is Result.Error -> {
                LoadResult.Error(Throwable(result.message))
            }
            is Result.Loading -> {
                LoadResult.Error(Throwable("Loading"))
            }
        }
    }
}