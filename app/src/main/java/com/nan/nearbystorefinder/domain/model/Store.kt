package com.nan.nearbystorefinder.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Store(
    val id: String = "",
    val name: String = "",
    val rating: Double = 0.0,
    val userRatingsTotal: Int = 0,
    val distance: Double = 0.0,
    val category: String = "",
    val imageUrl: String? = null,
    val isOpen: Boolean = false,
    val address: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
)
