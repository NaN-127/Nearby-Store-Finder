package com.nan.nearbystorefinder.presentation.detail.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nan.nearbystorefinder.domain.model.LocationState
import com.nan.nearbystorefinder.domain.model.PlaceDetailsResult
import com.nan.nearbystorefinder.domain.model.Store
import com.nan.nearbystorefinder.domain.repository.FavoriteRepository
import com.nan.nearbystorefinder.domain.repository.LocationRepository
import com.nan.nearbystorefinder.domain.repository.StoreRepository
import com.nan.nearbystorefinder.presentation.detail.state.PlaceDetailState
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class PlaceDetailViewModel(
    private val placeId: String,
    private val storeRepository: StoreRepository,
    private val favoriteRepository: FavoriteRepository,
    private val locationRepository: LocationRepository
) : ViewModel() {

    var state by mutableStateOf(PlaceDetailState())
        private set

    val locationState: StateFlow<LocationState> = locationRepository.state

    init {
        load()
    }

    fun refresh() = load(forceRefresh = true)

    fun retry() = load(forceRefresh = true)

    private fun load(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            if (forceRefresh || state.store == null) {
                state = state.copy(isLoading = true, error = null)
            }
            // Use whatever location is already known (don't force a fresh GPS fix
            // just to compute a detail-screen distance) - Home/Search already populate it.
            val location = (locationRepository.state.first() as? LocationState.Available)?.location
            val result = storeRepository.getPlaceDetails(
                placeId = placeId,
                userLat = location?.latitude,
                userLng = location?.longitude,
                forceRefresh = forceRefresh
            )
            when (result) {
                is PlaceDetailsResult.Success -> {
                    val isFav = favoriteRepository.favorites.first().any { it.id == result.store.id }
                    state = state.copy(store = result.store, isLoading = false, error = null, isFavorite = isFav)
                }
                is PlaceDetailsResult.Error -> {
                    state = state.copy(isLoading = false, error = result.error)
                }
            }
        }
    }

    fun toggleFavorite() {
        val store = state.store ?: return
        viewModelScope.launch {
            favoriteRepository.toggleFavorite(store)
            state = state.copy(isFavorite = !state.isFavorite)
        }
    }
}
