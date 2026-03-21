package com.nan.nearbystorefinder.core.di

import com.nan.nearbystorefinder.core.auth.GoogleAuthClient
import com.nan.nearbystorefinder.presentation.auth.viewmodel.AuthViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.nan.nearbystorefinder.core.location.LocationClient
import com.nan.nearbystorefinder.data.remote.repository.GeoapifyStoreRepository
import com.nan.nearbystorefinder.domain.repository.StoreRepository
import com.nan.nearbystorefinder.domain.repository.FavoriteRepository
import com.nan.nearbystorefinder.domain.repository.ProfileRepository
import com.nan.nearbystorefinder.presentation.home.viewmodel.HomeViewModel
import com.nan.nearbystorefinder.BuildConfig
import com.nan.nearbystorefinder.presentation.favorite.viewmodel.FavoriteViewModel
import com.nan.nearbystorefinder.presentation.profile.viewmodel.ProfileViewModel
import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {

    single {
        HttpClient(Android) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    coerceInputValues = true
                })
            }
        }
    }

    single<StoreRepository> {
        GeoapifyStoreRepository(get(), BuildConfig.GEOAPIFY_API_KEY)
    }

    single {
        FavoriteRepository(get(), get())
    }

    single {
        ProfileRepository(get(), get(), get())
    }

    single {
        FirebaseAuth.getInstance()
    }
    single {
        FirebaseFirestore.getInstance()
    }
    single {
        com.google.firebase.storage.FirebaseStorage.getInstance()
    }
    single {
        GoogleAuthClient(get())
    }

    single {
        LocationClient(androidContext())
    }
    viewModel {
        AuthViewModel(get(), get())
    }

    viewModel{
        HomeViewModel(get(), get(), get())
    }



    viewModel {
        FavoriteViewModel(get())
    }

    viewModel {
        ProfileViewModel(get(), get())
    }
}
