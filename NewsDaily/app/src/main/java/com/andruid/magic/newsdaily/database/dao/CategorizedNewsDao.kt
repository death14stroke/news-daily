package com.andruid.magic.newsdaily.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.andruid.magic.newsdaily.database.entity.CategorizedNews

@Dao
interface CategorizedNewsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(catNewsList: List<CategorizedNews>)

    @Query("SELECT * FROM categorized_news WHERE category = :category")
    fun getNewsIdForCat(category: String): List<CategorizedNews>
}