package com.nan.nearbystorefinder.core.di

import androidx.room.Room
import com.google.firebase.auth.FirebaseAuth
import com.nan.nearbystorefinder.BuildConfig
import com.nan.nearbystorefinder.core.auth.GoogleAuthClient
import com.nan.nearbystorefinder.core.location.LocationClient
import com.nan.nearbystorefinder.core.places.PlacesClientProvider
import com.nan.nearbystorefinder.data.local.database.AppDatabase
import com.nan.nearbystorefinder.data.local.repository.LocationRepositoryImpl
import com.nan.nearbystorefinder.data.remote.photo.PlacesPhotoLoader
import com.nan.nearbystorefinder.data.remote.repository.GooglePlacesStoreRepository
import com.nan.nearbystorefinder.domain.repository.FavoriteRepository
import com.nan.nearbystorefinder.domain.repository.LocationRepository
import com.nan.nearbystorefinder.domain.repository.ProfileRepository
import com.nan.nearbystorefinder.domain.repository.StoreRepository
import com.nan.nearbystorefinder.presentation.auth.viewmodel.AuthViewModel
import com.nan.nearbystorefinder.presentation.detail.viewmodel.PlaceDetailViewModel
import com.nan.nearbystorefinder.presentation.favorite.viewmodel.FavoriteViewModel
import com.nan.nearbystorefinder.presentation.home.viewmodel.HomeViewModel
import com.nan.nearbystorefinder.presentation.profile.viewmodel.ProfileViewModel
import com.nan.nearbystorefinder.presentation.search.viewmodel.SearchViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {

    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "nearo_db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    single { get<AppDatabase>().favoriteDao }

    // Places SDK for Android (New) - the API key is restricted to this app's
    // package name + SHA-1 in Google Cloud Console, so it's safe to ship in
    // the APK (unlike a raw REST API key, which can only be IP-restricted).
    single {
        PlacesClientProvider(androidContext(), BuildConfig.PLACES_API_KEY).client
    }

    single {
        PlacesPhotoLoader(get())
    }

    single<StoreRepository> {
        GooglePlacesStoreRepository(get(), get())
    }

    single<LocationRepository> {
        LocationRepositoryImpl(get())
    }

    single {
        FavoriteRepository(get())
    }

    single {
        ProfileRepository(get(), androidContext())
    }

    single {
        FirebaseAuth.getInstance()
    }

    single {
        GoogleAuthClient(get())
    }

    single<com.nan.nearbystorefinder.core.location.ILocationClient> {
        LocationClient(androidContext())
    }

    viewModel {
        AuthViewModel(get(), get(), androidContext(), get())
    }

    viewModel {
        HomeViewModel(get(), get(), get())
    }

    viewModel {
        SearchViewModel(get(), get(), get())
    }

    viewModel { params ->
        PlaceDetailViewModel(params.get(), get(), get(), get())
    }

    viewModel {
        FavoriteViewModel(get())
    }

    viewModel {
        ProfileViewModel(get(), get())
    }
}
