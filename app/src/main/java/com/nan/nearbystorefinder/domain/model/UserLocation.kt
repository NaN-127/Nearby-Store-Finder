package com.nan.nearbystorefinder.domain.model

import androidx.compose.runtime.Composable




data class UserLocation(
    val latitude: Double,
    val longitude: Double,
    val address: String = "Searching Address"
)
