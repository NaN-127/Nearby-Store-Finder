package com.nan.nearbystorefinder.domain.model

data class UserLocation(
    val latitude: Double,
    val longitude: Double,
    val address: String = "Searching Address"
)
