package com.nan.nearbystorefinder.presentation.favorite.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nan.nearbystorefinder.domain.model.Store
import com.nan.nearbystorefinder.domain.repository.FavoriteRepository
import com.nan.nearbystorefinder.presentation.favorite.state.FavoriteState
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class FavoriteViewModel(
    private val favoriteRepository: FavoriteRepository
) : ViewModel() {

    var state by mutableStateOf(FavoriteState())
        private set

    init {
        observeFavorites()
    }

    private fun observeFavorites() {
        viewModelScope.launch {
            android.util.Log.d("FavoriteViewModel", "Observing favorites")
            state = state.copy(isLoading = true)
            favoriteRepository.favorites.collectLatest { favoritesSet ->
                android.util.Log.d("FavoriteViewModel", "Collected ${favoritesSet.size} favorites")
                state = state.copy(
                    favorites = favoritesSet.toList(),
                    isLoading = false
                )
            }
        }
    }

    fun removeFavorite(store: Store) {
        viewModelScope.launch {
            favoriteRepository.removeFavorite(store)
        }
    }
}
