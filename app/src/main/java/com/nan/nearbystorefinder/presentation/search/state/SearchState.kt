package com.nan.nearbystorefinder.presentation.search.state

import com.nan.nearbystorefinder.domain.model.PlacesError
import com.nan.nearbystorefinder.domain.model.Store

data class SearchState(
    val query: String = "",
    val results: List<Store> = emptyList(),
    val isLoading: Boolean = false,
    val error: PlacesError? = null,
    val hasSearched: Boolean = false
)
