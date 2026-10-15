package com.nan.nearbystorefinder.fakes

import com.nan.nearbystorefinder.data.local.dao.FavoriteDao
import com.nan.nearbystorefinder.data.local.entity.StoreEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/** In-memory fake for [FavoriteDao] - no real Room database/instrumentation needed for ViewModel tests. */
class FakeFavoriteDao : FavoriteDao {
    private val _favorites = MutableStateFlow<List<StoreEntity>>(emptyList())
    val favoritesState: StateFlow<List<StoreEntity>> = _favorites

    override fun getAllFavorites() = _favorites

    override suspend fun insertFavorite(store: StoreEntity) {
        _favorites.value = _favorites.value.filterNot { it.id == store.id } + store
    }

    override suspend fun deleteFavorite(store: StoreEntity) {
        _favorites.value = _favorites.value.filterNot { it.id == store.id }
    }

    override suspend fun isFavorite(storeId: String): Boolean =
        _favorites.value.any { it.id == storeId }

    override suspend fun getFavoriteById(storeId: String): StoreEntity? =
        _favorites.value.firstOrNull { it.id == storeId }
}
