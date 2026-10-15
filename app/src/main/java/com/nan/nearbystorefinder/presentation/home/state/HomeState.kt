package com.nan.nearbystorefinder.presentation.home.state

import com.nan.nearbystorefinder.domain.model.PlaceCategory
import com.nan.nearbystorefinder.domain.model.PlacesError
import com.nan.nearbystorefinder.domain.model.Store

data class HomeState(
    val stores: List<Store> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val selectedCategory: PlaceCategory = PlaceCategory.ALL,
    val error: PlacesError? = null
)
