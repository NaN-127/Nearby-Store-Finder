package com.nan.nearbystorefinder.presentation.auth.viewmodel

import android.util.Patterns
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.nan.nearbystorefinder.presentation.auth.state.AuthState
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel(
    private val auth: FirebaseAuth
) : ViewModel() {

    var state by mutableStateOf(AuthState(user = auth.currentUser))
        private set

    init {
        auth.addAuthStateListener {
            state = state.copy(user = it.currentUser, isAuthReady = true)
        }
    }

    fun onEmailChange(email: String){
        state = state.copy(email = email, emailError = null)
    }

    fun onFullNameChange(fullName: String){
        state = state.copy(fullName = fullName, fullNameError = null)
    }

    fun onPasswordChange(password: String){
        state = state.copy(password = password, passwordError = null)
    }

    private fun validateLogin(): Boolean {
        var isValid = true
        if (state.email.isBlank()) {
            state = state.copy(emailError = "Email cannot be empty")
            isValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(state.email).matches()) {
            state = state.copy(emailError = "Please enter a valid email address")
            isValid = false
        }

        if (state.password.isBlank()) {
            state = state.copy(passwordError = "Password cannot be empty")
            isValid = false
        }

        return isValid
    }

    private fun validateSignUp(): Boolean {
        var isValid = true
        if (state.fullName.isBlank()) {
            state = state.copy(fullNameError = "Full Name cannot be empty")
            isValid = false
        }

        if (state.email.isBlank()) {
            state = state.copy(emailError = "Email cannot be empty")
            isValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(state.email).matches()) {
            state = state.copy(emailError = "Please enter a valid email address")
            isValid = false
        }

        if (state.password.isBlank()) {
            state = state.copy(passwordError = "Password cannot be empty")
            isValid = false
        } else if (state.password.length < 6) {
            state = state.copy(passwordError = "Password must be at least 6 characters")
            isValid = false
        }

        return isValid
    }

    fun login(){
        if (!validateLogin()) return
        
        viewModelScope.launch {
            state = state.copy(isLoading = true, error = null)
            try {
                val result = auth.signInWithEmailAndPassword(
                    state.email,
                    state.password
                ).await()

                state = state.copy(
                    isLoading = false,
                    user = result.user
                )

            } catch (e: Exception){
                state = state.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    fun signUp() {
        if (!validateSignUp()) return
        
        viewModelScope.launch {
            state = state.copy(isLoading = true, error = null)
            try {
                val result = auth.createUserWithEmailAndPassword(
                    state.email,
                    state.password
                ).await()

                state = state.copy(
                    isLoading = false,
                    user = result.user
                )

            } catch (e: Exception) {
                state = state.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    fun signInWithGoogle(idToken: String){
        viewModelScope.launch {
            state = state.copy(isLoading = true, error = null)

            try{
                val credential = GoogleAuthProvider.getCredential(idToken, null)

                val result = auth.signInWithCredential(credential).await()

                state = state.copy(
                    isLoading = false,
                    user = result.user
                )

            } catch (e: Exception){
                state = state.copy(
                    isLoading = false,
                    error = e.localizedMessage ?: e.message ?: "Google Sign-In failed"
                )
            }
        }
    }


    fun logout() {
        auth.signOut()
        state = state.copy(user = null)
    }
}