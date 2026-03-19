package com.nan.nearbystorefinder.presentation.home.state

import com.nan.nearbystorefinder.domain.model.Store


data class HomeState (
    val stores: List<Store> = emptyList(),
    val isLoading: Boolean = false
)