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

    @Query("SELECT offline_news.* FROM offline_news INNER JOIN categorized_news title_join ON offline_news.title = title_join.title INNER JOIN categorized_news source_join ON offline_news.sourceName = source_join.sourceName INNER JOIN categorized_news published_join ON offline_news.published = published_join.published WHERE title_join.category = :category")
    fun getNews(category: String): DataSource.Factory<Int, News>
}