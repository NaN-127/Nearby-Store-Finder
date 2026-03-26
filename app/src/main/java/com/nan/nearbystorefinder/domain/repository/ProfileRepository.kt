package com.nan.nearbystorefinder.domain.repository

import android.content.Context
import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class ProfileRepository(
    private val auth: FirebaseAuth,
    private val context: Context
) {
    private val sharedPrefs = context.getSharedPreferences("profile_prefs", Context.MODE_PRIVATE)

    private val _profileImageUri = MutableStateFlow<Uri?>(null)
    val profileImageUri = _profileImageUri.asStateFlow()

    private val _fullName = MutableStateFlow<String?>(null)
    val fullName = _fullName.asStateFlow()

    private val _email = MutableStateFlow<String?>(null)
    val email = _email.asStateFlow()

    suspend fun loadUserProfile() {
        val currentUser = auth.currentUser ?: return
        _email.value = currentUser.email
        
        val name = sharedPrefs.getString("full_name_${currentUser.uid}", currentUser.displayName)
        val photoUrl = sharedPrefs.getString("profile_photo_${currentUser.uid}", null)
        
        _fullName.value = name
        _profileImageUri.value = photoUrl?.let { Uri.parse(it) }
    }

    suspend fun updateProfileImage(uri: Uri) {
        val currentUser = auth.currentUser ?: return
        sharedPrefs.edit().putString("profile_photo_${currentUser.uid}", uri.toString()).apply()
        _profileImageUri.value = uri
    }

    suspend fun updateFullName(name: String) {
        val currentUser = auth.currentUser ?: return
        sharedPrefs.edit().putString("full_name_${currentUser.uid}", name).apply()
        _fullName.value = name
    }
}
