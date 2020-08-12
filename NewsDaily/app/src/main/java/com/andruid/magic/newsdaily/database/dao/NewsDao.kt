package com.andruid.magic.newsdaily.database.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.andruid.magic.newsdaily.database.entity.NewsItem
import com.andruid.magic.newsdaily.database.entity.ReadNews
import kotlinx.coroutines.flow.Flow

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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReadNews(readNews: ReadNews)

    @Query("SELECT COUNT(*) FROM read_news WHERE url = :url AND category = :category")
    suspend fun findRead(category: String, url: String): Int

    @Query("SELECT COUNT(*) FROM offline_news WHERE category = :category")
    fun countTotal(category: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM read_news WHERE category = :category")
    fun countRead(category: String): Flow<Int>
}