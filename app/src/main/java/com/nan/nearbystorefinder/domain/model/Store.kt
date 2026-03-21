package com.nan.nearbystorefinder.domain.model



data class Store(
    val id: String = "",
    val name: String = "",
    val rating: Float = 0f,
    val userRatingsTotal: Int = 0,
    val distance: Float = 0f,
    val category: String = "",
    val imageUrl: String? = null,
    val isOpen: Boolean = false,
    val address: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
)
