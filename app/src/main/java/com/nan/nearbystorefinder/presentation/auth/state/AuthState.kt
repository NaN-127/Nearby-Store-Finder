package com.nan.nearbystorefinder.presentation.auth.state

import com.google.firebase.auth.FirebaseUser

data class AuthState(
    val fullName: String = "",
    val fullNameError: String? = null,
    val email: String = "",
    val emailError: String? = null,
    val password: String = "",
    val passwordError: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val user: FirebaseUser? = null,
    val isGuest: Boolean = false,
    val isAuthReady: Boolean = false
)
