package com.nan.nearbystorefinder.presentation.auth.state




data class AuthState(
    val fullName: String = "",
    val fullNameError: String? = null,
    val email: String = "",
    val emailError: String? = null,
    val password: String = "",
    val passwordError: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val user: com.google.firebase.auth.FirebaseUser? = null,
    val isAuthReady: Boolean = false
)
