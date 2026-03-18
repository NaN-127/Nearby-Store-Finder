package com.nan.nearbystorefinder.presentation.auth.state




data class AuthState(
    val fullName: String = "",
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val user: com.google.firebase.auth.FirebaseUser? = null
)
