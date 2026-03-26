package com.nan.nearbystorefinder.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.nan.nearbystorefinder.data.local.dao.FavoriteDao
import com.nan.nearbystorefinder.data.local.entity.StoreEntity

@Database(entities = [StoreEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract val favoriteDao: FavoriteDao
}
