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
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
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
        auth.signOut()
        state = AuthState(user = null)
    }

    fun login(){
        viewModelScope.launch {
            state = state.copy(isLoading = true, error = null, isAuthReady = false)
            try {
                val result = auth.signInWithEmailAndPassword(
                    state.email,
                    state.password
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
                    state.email,
                    state.password
                ).await()

                result.user?.let { user ->
                    val userData = mapOf(
                        "uid" to user.uid,
                        "email" to state.email,
                        "fullName" to state.fullName,
                        "profilePhotoUrl" to null
                    )
                    firestore.collection("users").document(user.uid).set(userData).await()
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
                        val userData = mapOf(
                            "uid" to user.uid,
                            "email" to user.email,
                            "fullName" to (user.displayName ?: ""),
                            "profilePhotoUrl" to (user.photoUrl?.toString())
                        )
                        firestore.collection("users").document(user.uid).set(userData).await()
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



