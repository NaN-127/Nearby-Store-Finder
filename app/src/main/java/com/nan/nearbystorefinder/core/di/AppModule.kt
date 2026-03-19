package com.nan.nearbystorefinder.core.di

import com.nan.nearbystorefinder.core.auth.GoogleAuthClient
import com.nan.nearbystorefinder.presentation.auth.viewmodel.AuthViewModel
import com.google.firebase.auth.FirebaseAuth
import com.nan.nearbystorefinder.domain.repository.FakeStoreRepository
import com.nan.nearbystorefinder.domain.repository.StoreRepository
import com.nan.nearbystorefinder.presentation.home.viewmodel.HomeViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module


val appModule = module {

    single<StoreRepository> {
        FakeStoreRepository()
    }
    single {
        FirebaseAuth.getInstance()
    }
    single {
        GoogleAuthClient(get())
    }
    viewModel {
        AuthViewModel(get())
    }

    viewModel{
        HomeViewModel(get())
    }
}
