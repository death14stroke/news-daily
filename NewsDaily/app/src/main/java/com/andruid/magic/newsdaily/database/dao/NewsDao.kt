package com.andruid.magic.newsdaily.database.dao

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.andruid.magic.newsdaily.database.entity.News

@Dao
interface NewsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(newsList: List<News>)

    @Query("SELECT news.* FROM categorized_news LEFT JOIN offline_news as news WHERE category = :category")
    fun getNews(category: String) : DataSource.Factory<Int, News>
}