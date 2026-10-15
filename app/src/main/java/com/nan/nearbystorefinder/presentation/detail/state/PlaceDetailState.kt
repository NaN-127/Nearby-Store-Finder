package com.nan.nearbystorefinder.presentation.detail.state

import com.nan.nearbystorefinder.domain.model.PlacesError
import com.nan.nearbystorefinder.domain.model.Store

data class PlaceDetailState(
    val store: Store? = null,
    val isLoading: Boolean = true,
    val error: PlacesError? = null,
    val isFavorite: Boolean = false
)
