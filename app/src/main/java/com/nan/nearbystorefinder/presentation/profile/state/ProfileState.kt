package com.nan.nearbystorefinder.presentation.profile.state

data class ProfileState(
    val fullName: String = "",
    val email: String = "",
    val isPremium: Boolean = false,
    val placesSaved: Int = 0,
    val points: String = "0",
    val isLoading: Boolean = false,
    val error: String? = null
)
