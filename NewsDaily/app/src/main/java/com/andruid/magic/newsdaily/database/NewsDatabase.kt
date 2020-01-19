package com.andruid.magic.newsdaily.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.andruid.magic.newsdaily.database.dao.CategorizedNewsDao
import com.andruid.magic.newsdaily.database.dao.NewsDao
import com.andruid.magic.newsdaily.database.entity.CategorizedNews
import com.andruid.magic.newsdaily.database.entity.News

@Database(entities = [News::class, CategorizedNews::class], version = 1)
abstract class NewsDatabase : RoomDatabase() {
    companion object {
        private const val DATABASE_NAME = "news_db"
        private val LOCK = Any()

        private lateinit var INSTANCE: NewsDatabase

        @JvmStatic
        fun getInstance(context: Context): NewsDatabase {
            if (!::INSTANCE.isInitialized) {
                synchronized(LOCK) {
                    INSTANCE = Room.databaseBuilder(
                        context.applicationContext,
                        NewsDatabase::class.java, DATABASE_NAME
                    ).build()
                }
            }
            return INSTANCE
        }
    }

    abstract fun newsDao(): NewsDao

    abstract fun catNewsDao(): CategorizedNewsDao
}