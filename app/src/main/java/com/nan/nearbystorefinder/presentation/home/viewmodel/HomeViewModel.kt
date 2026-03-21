package com.nan.nearbystorefinder.presentation.home.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nan.nearbystorefinder.core.location.LocationClient
import com.nan.nearbystorefinder.domain.model.Store
import com.nan.nearbystorefinder.domain.model.UserLocation
import com.nan.nearbystorefinder.domain.repository.FavoriteRepository
import com.nan.nearbystorefinder.domain.repository.StoreRepository
import com.nan.nearbystorefinder.presentation.home.state.HomeState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(
    private val locationClient: LocationClient,
    private val storeRepository: StoreRepository,
    private val favoriteRepository: FavoriteRepository
): ViewModel() {

    var userLocation by mutableStateOf<UserLocation?>(null)
        private set

    var state by mutableStateOf(HomeState())
        private set

    val favoriteStoreIds: StateFlow<Set<String>> = favoriteRepository.favorites
        .map { 
            val ids = it.map { store -> store.id }.toSet()
            android.util.Log.d("HomeViewModel", "Favorites updated: ${ids.size} IDs")
            ids
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    init {
        fetchUserLocation()
    }

    fun onCategorySelected(category: String) {
        state = state.copy(selectedCategory = category)
        fetchNearbyStores(category)
    }

    fun toggleFavorite(store: Store) {
        viewModelScope.launch {
            favoriteRepository.toggleFavorite(store)
        }
    }

    fun fetchUserLocation(){
        viewModelScope.launch {
            val coords = locationClient.getCurrentLocation()
            if(coords != null){
                val addressName = locationClient.getAddressFromCoords(coords.latitude, coords.longitude)
                userLocation = coords.copy(address = addressName ?: "Current Location")
            } else {
                userLocation = UserLocation(40.7128, -74.0060, "New York (Default)")
            }
            fetchNearbyStores()
        }
    }

    private fun fetchNearbyStores(category: String? = state.selectedCategory) {
        val location = userLocation ?: return
        viewModelScope.launch {
            state = state.copy(isLoading = true)
            try {
                val stores = storeRepository.getNearbyStores(
                    lat = location.latitude,
                    lng = location.longitude,
                    category = category
                )
                state = state.copy(
                    stores = stores,
                    isLoading = false
                )
            } catch (e: Exception) {
                state = state.copy(isLoading = false)
            }
        }
    }
}
