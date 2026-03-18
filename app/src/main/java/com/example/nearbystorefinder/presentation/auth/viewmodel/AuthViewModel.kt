package com.example.nearbystorefinder.presentation.auth.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nearbystorefinder.presentation.auth.state.AuthState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel(
    private val auth: FirebaseAuth
): ViewModel() {

    var state by mutableStateOf(AuthState())
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

    fun login(){
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

            }catch (e: Exception){
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


            }catch (e: Exception){
                state = state.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }



    }


}