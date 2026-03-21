package com.nan.nearbystorefinder.presentation.favorite.state

import com.nan.nearbystorefinder.domain.model.Store

data class FavoriteState(
    val favorites: List<Store> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
