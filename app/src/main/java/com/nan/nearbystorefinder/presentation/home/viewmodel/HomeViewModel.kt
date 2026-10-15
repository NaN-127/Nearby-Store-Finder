package com.nan.nearbystorefinder.presentation.home.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nan.nearbystorefinder.domain.model.LocationState
import com.nan.nearbystorefinder.domain.model.PlaceCategory
import com.nan.nearbystorefinder.domain.model.PlacesResult
import com.nan.nearbystorefinder.domain.model.Store
import com.nan.nearbystorefinder.domain.repository.FavoriteRepository
import com.nan.nearbystorefinder.domain.repository.LocationRepository
import com.nan.nearbystorefinder.domain.repository.StoreRepository
import com.nan.nearbystorefinder.domain.util.DistanceCalculator
import com.nan.nearbystorefinder.presentation.home.state.HomeState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Default search radius for Home's nearby feed. Kept as a single constant so it's easy to make configurable later. */
private const val DEFAULT_RADIUS_METERS = 5000
private const val DEFAULT_MAX_RESULTS = 20
private const val LOCATION_CHANGE_THRESHOLD_METERS = 100.0

class HomeViewModel(
    private val locationRepository: LocationRepository,
    private val storeRepository: StoreRepository,
    private val favoriteRepository: FavoriteRepository
) : ViewModel() {

    var state by mutableStateOf(HomeState())
        private set

    val locationState: StateFlow<LocationState> = locationRepository.state

    val favoriteStoreIds: StateFlow<Set<String>> = favoriteRepository.favorites
        .map { favorites -> favorites.map { it.id }.toSet() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    // Tracks the in-flight search so a newer request can cancel a stale one -
    // e.g. rapidly tapping between category chips must not let an older,
    // slower response overwrite the result of a more recent selection.
    private var searchJob: Job? = null

    private var lastFetchedLat: Double? = null
    private var lastFetchedLng: Double? = null
    private var lastFetchedCategory: PlaceCategory? = null

    init {
        viewModelScope.launch {
            locationRepository.state.collect { locationState ->
                if (locationState is LocationState.Available) {
                    val loc = locationState.location
                    val prevLat = lastFetchedLat
                    val prevLng = lastFetchedLng
                    val categoryMatches = lastFetchedCategory == state.selectedCategory
                    val locationUnchanged = prevLat != null && prevLng != null &&
                        (DistanceCalculator.distanceKm(prevLat, prevLng, loc.latitude, loc.longitude) * 1000.0) < LOCATION_CHANGE_THRESHOLD_METERS

                    val hasLoadedForCurrentInputs = categoryMatches && locationUnchanged && state.error == null

                    if (!hasLoadedForCurrentInputs) {
                        fetchNearbyStores(loc.latitude, loc.longitude, forceRefresh = false)
                    }
                }
            }
        }
        refreshLocation()
    }

    fun refreshLocation(force: Boolean = false) {
        viewModelScope.launch { locationRepository.refreshLocation(force) }
    }

    fun onLocationPermissionDenied() {
        locationRepository.onPermissionDenied()
    }

    fun onCategorySelected(category: PlaceCategory) {
        if (category == state.selectedCategory && lastFetchedCategory == category && state.error == null) {
            return
        }
        state = state.copy(selectedCategory = category)
        val location = (locationState.value as? LocationState.Available)?.location ?: return
        fetchNearbyStores(location.latitude, location.longitude, forceRefresh = false)
    }

    fun refresh() {
        val location = (locationState.value as? LocationState.Available)?.location
        if (location != null) {
            fetchNearbyStores(location.latitude, location.longitude, forceRefresh = true)
        } else {
            refreshLocation(force = true)
        }
    }

    fun retry() {
        refresh()
    }

    fun toggleFavorite(store: Store) {
        viewModelScope.launch {
            favoriteRepository.toggleFavorite(store)
        }
    }

    private fun fetchNearbyStores(lat: Double, lng: Double, forceRefresh: Boolean = false) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            if (forceRefresh) {
                state = state.copy(isRefreshing = true, error = null)
            } else if (state.stores.isEmpty()) {
                state = state.copy(isLoading = true, error = null)
            }

            val requestedCategory = state.selectedCategory
            val result = storeRepository.getNearbyStores(
                lat = lat,
                lng = lng,
                radiusMeters = DEFAULT_RADIUS_METERS,
                category = requestedCategory,
                maxResults = DEFAULT_MAX_RESULTS,
                sortByDistance = true,
                forceRefresh = forceRefresh
            )
            state = when (result) {
                is PlacesResult.Success -> {
                    lastFetchedLat = lat
                    lastFetchedLng = lng
                    lastFetchedCategory = requestedCategory
                    state.copy(
                        stores = result.stores.sortedBy { it.distanceKm ?: Double.MAX_VALUE },
                        isLoading = false,
                        isRefreshing = false,
                        error = null
                    )
                }
                is PlacesResult.Error -> {
                    // Preserve existing stores on failure, surface error
                    state.copy(
                        isLoading = false,
                        isRefreshing = false,
                        error = result.error
                    )
                }
            }
        }
    }
}
