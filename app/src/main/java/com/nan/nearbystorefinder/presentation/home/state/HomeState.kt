package com.nan.nearbystorefinder.presentation.home.state

import com.nan.nearbystorefinder.domain.model.Store
import com.nan.nearbystorefinder.domain.model.UserLocation


data class HomeState (
    val stores: List<Store> = emptyList(),
    val isLoading: Boolean = false,
)