package com.andruid.magic.newsdaily.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.andruid.magic.newsdaily.database.entity.News
import kotlinx.coroutines.flow.Flow

@Dao
interface NewsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(newsList: List<News>)

    @Query("SELECT * FROM offline_news WHERE sourceName = :sourceName AND title = :title AND published = :published")
    fun getNews(sourceName: String, title: String, published: Long): Flow<List<News>>
}