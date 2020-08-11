package com.andruid.magic.newsdaily.database.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.andruid.magic.newsdaily.database.entity.NewsItem

@Dao
interface NewsDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(newsItems: List<NewsItem>)

    @Query("SELECT * FROM offline_news WHERE country = :country AND category = :category ORDER BY published DESC")
    fun getNewsForCategory(
        country: String,
        category: String
    ): PagingSource<Int, NewsItem>

    @Query("SELECT * FROM offline_news WHERE country = :country AND category = :category ORDER BY published DESC LIMIT :size OFFSET :offset")
    fun getNewsForCategoryPage(
        country: String,
        category: String,
        offset: Int,
        size: Int
    ): List<NewsItem>

    /*@Query("UPDATE offline_news SET unread = 0 WHERE category = :category AND url = :url")
    fun markNewsAsRead(category: String, url: String)*/
}