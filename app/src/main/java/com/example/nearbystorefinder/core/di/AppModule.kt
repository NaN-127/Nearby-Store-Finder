package com.example.nearbystorefinder.core.di

import com.example.nearbystorefinder.core.auth.GoogleAuthClient
import com.example.nearbystorefinder.presentation.auth.viewmodel.AuthViewModel
import com.google.firebase.auth.FirebaseAuth
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module


val appModule = module {
    single {
        FirebaseAuth.getInstance()
    }
    single {
        GoogleAuthClient(get())
    }
    viewModel {
        AuthViewModel(get())
    }
}