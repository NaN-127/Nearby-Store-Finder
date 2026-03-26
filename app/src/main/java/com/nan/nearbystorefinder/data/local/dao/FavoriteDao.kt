package com.nan.nearbystorefinder.data.local.dao

import androidx.room.*
import com.nan.nearbystorefinder.data.local.entity.StoreEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {
    @Query("SELECT * FROM favorites")
    fun getAllFavorites(): Flow<List<StoreEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(store: StoreEntity)

    @Delete
    suspend fun deleteFavorite(store: StoreEntity)

    @Query("SELECT EXISTS(SELECT * FROM favorites WHERE id = :storeId)")
    suspend fun isFavorite(storeId: String): Boolean

    @Query("SELECT * FROM favorites WHERE id = :storeId")
    suspend fun getFavoriteById(storeId: String): StoreEntity?
}
