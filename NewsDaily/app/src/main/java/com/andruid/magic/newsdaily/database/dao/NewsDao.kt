package com.andruid.magic.newsdaily.database.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.andruid.magic.newsdaily.database.entity.NewsItem

@Dao
interface NewsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(newsItems: List<NewsItem>)

    @Query("SELECT * FROM offline_news WHERE country = :country AND category = :category ORDER BY published DESC")
    fun getNewsForCategory(
        country: String,
        category: String
    ): PagingSource<Int, NewsItem>

    @Query("SELECT * FROM offline_news WHERE country = :country AND category = :category ORDER BY published DESC LIMIT :size OFFSET :offset")
    suspend fun getNewsForCategoryPage(
        country: String,
        category: String,
        offset: Int,
        size: Int
    ): List<NewsItem>

    @Query("SELECT published FROM offline_news ORDER BY published DESC LIMIT 1")
    suspend fun getLatestNewsTime(): Long
}