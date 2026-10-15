package com.nan.nearbystorefinder.presentation.search.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nan.nearbystorefinder.domain.model.LocationState
import com.nan.nearbystorefinder.domain.model.PlacesResult
import com.nan.nearbystorefinder.domain.model.Store
import com.nan.nearbystorefinder.domain.repository.FavoriteRepository
import com.nan.nearbystorefinder.domain.repository.LocationRepository
import com.nan.nearbystorefinder.domain.repository.StoreRepository
import com.nan.nearbystorefinder.presentation.search.state.SearchState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private const val DEFAULT_RADIUS_METERS = 10000
private const val DEFAULT_MAX_RESULTS = 20
private const val DEBOUNCE_MS = 350L

class SearchViewModel(
    private val storeRepository: StoreRepository,
    private val locationRepository: LocationRepository,
    private val favoriteRepository: FavoriteRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SearchState())
    val state: StateFlow<SearchState> = _state.asStateFlow()

    val locationState: StateFlow<LocationState> = locationRepository.state

    val favoriteStoreIds: StateFlow<Set<String>> = favoriteRepository.favorites
        .map { favorites -> favorites.map { it.id }.toSet() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    private var debounceJob: Job? = null
    private var searchJob: Job? = null

    fun onQueryChange(query: String) {
        _state.value = _state.value.copy(query = query)
        debounceJob?.cancel()
        if (query.isBlank()) {
            searchJob?.cancel()
            _state.value = _state.value.copy(results = emptyList(), isLoading = false, hasSearched = false, error = null)
            return
        }
        debounceJob = viewModelScope.launch {
            delay(DEBOUNCE_MS)
            runSearch(query)
        }
    }

    fun toggleFavorite(store: Store) {
        viewModelScope.launch {
            favoriteRepository.toggleFavorite(store)
        }
    }

    fun refresh() {
        val query = _state.value.query
        if (query.isNotBlank()) runSearch(query, forceRefresh = true)
    }

    fun retry() {
        val query = _state.value.query
        if (query.isNotBlank()) runSearch(query, forceRefresh = true)
    }

    private fun runSearch(query: String, forceRefresh: Boolean = false) {
        val location = (locationState.value as? LocationState.Available)?.location
        if (location == null) {
            _state.value = _state.value.copy(isLoading = false, hasSearched = true, error = null, results = emptyList())
            return
        }

        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            val result = storeRepository.searchStores(
                query = query,
                lat = location.latitude,
                lng = location.longitude,
                radiusMeters = DEFAULT_RADIUS_METERS,
                maxResults = DEFAULT_MAX_RESULTS,
                forceRefresh = forceRefresh
            )
            _state.value = when (result) {
                is PlacesResult.Success -> _state.value.copy(
                    results = result.stores.sortedBy { it.distanceKm ?: Double.MAX_VALUE },
                    isLoading = false,
                    hasSearched = true,
                    error = null
                )
                is PlacesResult.Error -> _state.value.copy(
                    isLoading = false,
                    hasSearched = true,
                    error = result.error
                )
            }
        }
    }
}
