package com.nan.nearbystorefinder.core.di

import com.nan.nearbystorefinder.core.auth.GoogleAuthClient
import com.nan.nearbystorefinder.presentation.auth.viewmodel.AuthViewModel
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
