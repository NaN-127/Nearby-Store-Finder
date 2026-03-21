package com.nan.nearbystorefinder.presentation.profile.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nan.nearbystorefinder.domain.repository.FavoriteRepository
import com.nan.nearbystorefinder.domain.repository.ProfileRepository
import com.nan.nearbystorefinder.presentation.profile.state.ProfileState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val profileRepository: ProfileRepository,
    private val favoriteRepository: FavoriteRepository
) : ViewModel() {

    val profileImageUri: StateFlow<Uri?> = profileRepository.profileImageUri
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val fullName: StateFlow<String?> = profileRepository.fullName
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _favoritesCount = MutableStateFlow(0)
    val favoritesCount = _favoritesCount.asStateFlow()

    init {
        viewModelScope.launch {
            profileRepository.loadUserProfile()
        }
        viewModelScope.launch {
            favoriteRepository.favorites.collectLatest {
                _favoritesCount.value = it.size
            }
        }
    }

    fun updateProfileImage(uri: Uri) {
        android.util.Log.d("ProfileViewModel", "Updating profile image with URI: $uri")
        viewModelScope.launch {
            profileRepository.updateProfileImage(uri)
        }
    }
}
