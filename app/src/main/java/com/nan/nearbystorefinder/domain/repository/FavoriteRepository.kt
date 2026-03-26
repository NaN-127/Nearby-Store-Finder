package com.nan.nearbystorefinder.domain.repository

import com.nan.nearbystorefinder.data.local.dao.FavoriteDao
import com.nan.nearbystorefinder.data.local.entity.toStore
import com.nan.nearbystorefinder.data.local.entity.toStoreEntity
import com.nan.nearbystorefinder.domain.model.Store
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FavoriteRepository(
    private val favoriteDao: FavoriteDao
) {
    val favorites: Flow<Set<Store>> = favoriteDao.getAllFavorites()
        .map { entities -> 
            entities.map { it.toStore() }.toSet() 
        }

    suspend fun toggleFavorite(store: Store) {
        val isFav = favoriteDao.isFavorite(store.id)
        if (isFav) {
            favoriteDao.deleteFavorite(store.toStoreEntity())
        } else {
            favoriteDao.insertFavorite(store.toStoreEntity())
        }
    }

    suspend fun removeFavorite(store: Store) {
        favoriteDao.deleteFavorite(store.toStoreEntity())
    }
}
