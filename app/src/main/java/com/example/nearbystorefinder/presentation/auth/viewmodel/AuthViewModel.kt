package com.example.nearbystorefinder.presentation.auth.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.nearbystorefinder.presentation.auth.state.AuthState

class AuthViewModel: ViewModel() {

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

    }


}