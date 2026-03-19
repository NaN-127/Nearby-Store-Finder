package com.nan.nearbystorefinder.domain.model



data class Store(
    val id: String,
    val name: String,
    val rating: Float,
    val distance: Float,
    val category: String,
    val imageUrl: String,
    val isOpen: Boolean
)