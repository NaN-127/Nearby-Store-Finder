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
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel(
    private val auth: FirebaseAuth,
    private val googleAuthClient: GoogleAuthClient,
    private val context: Context
): ViewModel() {

    private val sharedPrefs = context.getSharedPreferences("profile_prefs", Context.MODE_PRIVATE)

    var state by mutableStateOf(AuthState(user = auth.currentUser))
        private set

    fun onEmailChange(email: String){
        state = state.copy(email = email)
    }

    fun onFullNameChange(fullName: String){
        state = state.copy(fullName = fullName)
    }

    fun onPasswordChange(password: String){
        state = state.copy(password = password)
    }

    fun logout() {
        viewModelScope.launch {
            auth.signOut()
            googleAuthClient.signOut()
            state = AuthState()
        }
    }

    fun clearError() {
        state = state.copy(error = null)
    }

    fun login(){
        viewModelScope.launch {
            state = state.copy(isLoading = true, error = null, isAuthReady = false)
            try {
                val result = auth.signInWithEmailAndPassword(
                    state.email.trim(),
                    state.password.trim()
                ).await()

                state = state.copy(
                    isLoading = false,
                    user = result.user,
                    isAuthReady = true
                )

            }catch (e: Exception){
                state = state.copy(
                    isLoading = false,
                    error = e.message,
                    isAuthReady = false
                )
            }
        }
    }

    fun signUp() {
        viewModelScope.launch {
            state = state.copy(isLoading = true, error = null, isAuthReady = false)
            try {
                val result = auth.createUserWithEmailAndPassword(
                    state.email.trim(),
                    state.password.trim()
                ).await()

                result.user?.let { user ->
                    sharedPrefs.edit()
                        .putString("full_name_${user.uid}", state.fullName)
                        .apply()
                }

                state = state.copy(
                    isLoading = false,
                    user = result.user,
                    isAuthReady = true
                )

            } catch (e: Exception) {
                state = state.copy(
                    isLoading = false,
                    error = e.message,
                    isAuthReady = false
                )
            }
        }
    }

    fun signInWithGoogle(idToken: String){
        viewModelScope.launch {
            state = state.copy(isLoading = true, error = null, isAuthReady = false)

            try{
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
                    isAuthReady = true
                )
            }catch (e: Exception){
                state = state.copy(
                    isLoading = false,
                    error = e.message,
                    isAuthReady = false
                )
            }
        }
    }
}
