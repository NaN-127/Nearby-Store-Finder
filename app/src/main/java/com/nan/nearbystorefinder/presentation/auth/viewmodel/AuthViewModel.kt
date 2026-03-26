package com.nan.nearbystorefinder.presentation.auth.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nan.nearbystorefinder.presentation.auth.state.AuthState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.nan.nearbystorefinder.core.auth.GoogleAuthClient
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val googleAuthClient: GoogleAuthClient
): ViewModel() {

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
                    val userData = mutableMapOf(
                        "uid" to user.uid,
                        "email" to state.email,
                        "fullName" to state.fullName,
                        "profilePhotoUrl" to null
                    )
                    
                    var retryCount = 0
                    var success = false
                    while (retryCount < 3 && !success) {
                        try {
                            firestore.collection("users").document(user.uid).set(userData).await()
                            success = true
                        } catch (e: Exception) {
                            retryCount++
                            if (retryCount >= 3) throw e
                            kotlinx.coroutines.delay(500)
                        }
                    }
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

                state = state.copy(
                    user = result.user
                )

                result.user?.let { user ->
                    val userDoc = firestore.collection("users").document(user.uid).get().await()
                    if (!userDoc.exists()) {
                        val userData = mutableMapOf(
                            "uid" to user.uid,
                            "email" to user.email,
                            "fullName" to (user.displayName ?: ""),
                            "profilePhotoUrl" to (user.photoUrl?.toString())
                        )
                        
                        var retryCount = 0
                        var success = false
                        while (retryCount < 3 && !success) {
                            try {
                                firestore.collection("users").document(user.uid).set(userData).await()
                                success = true
                            } catch (e: Exception) {
                                retryCount++
                                if (retryCount >= 3) throw e
                                kotlinx.coroutines.delay(500)
                            }
                        }
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



