package com.example.nearbystorefinder.core.di

import com.example.nearbystorefinder.presentation.auth.viewmodel.AuthViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module


val appModule = module {
    viewModel {
        AuthViewModel()
    }
}