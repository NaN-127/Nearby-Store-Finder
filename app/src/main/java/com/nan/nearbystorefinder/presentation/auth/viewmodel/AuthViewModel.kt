package com.nan.nearbystorefinder.presentation.auth.viewmodel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nan.nearbystorefinder.presentation.auth.state.AuthState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.nan.nearbystorefinder.core.auth.GoogleAuthClient
import com.nan.nearbystorefinder.domain.repository.StoreRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel(
    private val auth: FirebaseAuth,
    private val googleAuthClient: GoogleAuthClient,
    private val context: Context,
    private val storeRepository: StoreRepository? = null
): ViewModel() {

    private val sharedPrefs = context.getSharedPreferences("profile_prefs", Context.MODE_PRIVATE)

    var state by mutableStateOf(AuthState(user = auth.currentUser))
        private set

    fun onEmailChange(email: String) {
        state = state.copy(email = email, emailError = null)
    }

    fun onFullNameChange(fullName: String) {
        state = state.copy(fullName = fullName, fullNameError = null)
    }

    fun onPasswordChange(password: String) {
        state = state.copy(password = password, passwordError = null)
    }

    fun continueAsGuest() {
        state = state.copy(isGuest = true, isAuthReady = true, error = null)
    }

    fun logout() {
        viewModelScope.launch {
            auth.signOut()
            googleAuthClient.signOut()
            storeRepository?.clearCache()
            state = AuthState(user = null, isGuest = false)
        }
    }

    fun clearError() {
        state = state.copy(error = null)
    }

    fun login() {
        val trimmedEmail = state.email.trim()
        val trimmedPassword = state.password.trim()

        if (trimmedEmail.isEmpty()) {
            state = state.copy(error = "Please enter your email address")
            return
        }
        if (trimmedPassword.isEmpty()) {
            state = state.copy(error = "Please enter your password")
            return
        }

        viewModelScope.launch {
            state = state.copy(isLoading = true, error = null, isAuthReady = false)
            try {
                val result = auth.signInWithEmailAndPassword(trimmedEmail, trimmedPassword).await()
                state = state.copy(
                    isLoading = false,
                    user = result.user,
                    isGuest = false,
                    isAuthReady = true
                )
            } catch (e: Exception) {
                state = state.copy(
                    isLoading = false,
                    error = e.localizedMessage ?: "Login failed. Please verify credentials.",
                    isAuthReady = false
                )
            }
        }
    }

    fun signUp() {
        val trimmedName = state.fullName.trim()
        val trimmedEmail = state.email.trim()
        val trimmedPassword = state.password.trim()

        if (trimmedName.isEmpty()) {
            state = state.copy(error = "Please enter your full name")
            return
        }
        if (trimmedEmail.isEmpty()) {
            state = state.copy(error = "Please enter your email address")
            return
        }
        if (trimmedPassword.length < 6) {
            state = state.copy(error = "Password must be at least 6 characters")
            return
        }

        viewModelScope.launch {
            state = state.copy(isLoading = true, error = null, isAuthReady = false)
            try {
                val result = auth.createUserWithEmailAndPassword(trimmedEmail, trimmedPassword).await()
                result.user?.let { user ->
                    sharedPrefs.edit()
                        .putString("full_name_${user.uid}", trimmedName)
                        .apply()
                }
                state = state.copy(
                    isLoading = false,
                    user = result.user,
                    isGuest = false,
                    isAuthReady = true
                )
            } catch (e: Exception) {
                state = state.copy(
                    isLoading = false,
                    error = e.localizedMessage ?: "Sign up failed.",
                    isAuthReady = false
                )
            }
        }
    }

    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            state = state.copy(isLoading = true, error = null, isAuthReady = false)
            try {
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                val result = auth.signInWithCredential(credential).await()
                result.user?.let { user ->
                    if (sharedPrefs.getString("full_name_${user.uid}", null) == null) {
                        sharedPrefs.edit()
                            .putString("full_name_${user.uid}", user.displayName ?: "")
                            .putString("profile_photo_${user.uid}", user.photoUrl?.toString())
                            .apply()
                    }
                }
                state = state.copy(
                    isLoading = false,
                    user = result.user,
                    isGuest = false,
                    isAuthReady = true
                )
            } catch (e: Exception) {
                state = state.copy(
                    isLoading = false,
                    error = e.localizedMessage ?: "Google sign in failed.",
                    isAuthReady = false
                )
            }
        }
    }
}
