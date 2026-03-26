package com.nan.nearbystorefinder.domain.repository

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

class ProfileRepository(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val storage: com.google.firebase.storage.FirebaseStorage
) {
    private val _profileImageUri = MutableStateFlow<Uri?>(null)
    val profileImageUri = _profileImageUri.asStateFlow()

    private val _fullName = MutableStateFlow<String?>(null)
    val fullName = _fullName.asStateFlow()

    private val _email = MutableStateFlow<String?>(null)
    val email = _email.asStateFlow()

    suspend fun loadUserProfile() {
        val currentUser = auth.currentUser ?: return
        _email.value = currentUser.email
        try {
            val document = firestore.collection("users").document(currentUser.uid).get().await()
            if (document.exists()) {
                val name = document.getString("fullName")
                val photoUrl = document.getString("profilePhotoUrl")
                _fullName.value = name
                _profileImageUri.value = photoUrl?.let { Uri.parse(it) }
            }
        } catch (e: Exception) {
            android.util.Log.e("ProfileRepository", "Error loading profile: ${e.message}", e)
        }
    }

    suspend fun updateProfileImage(uri: Uri) {
        val currentUser = auth.currentUser ?: return
        try {
            android.util.Log.d("ProfileRepository", "Starting image upload for user: ${currentUser.uid}")
            val ref = storage.reference.child("profile_photos/${currentUser.uid}.jpg")
            ref.putFile(uri).await()
            val downloadUrl = ref.downloadUrl.await()
            android.util.Log.d("ProfileRepository", "Upload successful, downloadUrl: $downloadUrl")

            firestore.collection("users").document(currentUser.uid)
                .update("profilePhotoUrl", downloadUrl.toString()).await()
            android.util.Log.d("ProfileRepository", "Firestore updated with new photo URL")

            _profileImageUri.value = downloadUrl
        } catch (e: Exception) {
            android.util.Log.e("ProfileRepository", "Error updating profile image: ${e.message}", e)
        }
    }

    suspend fun updateFullName(name: String) {
        val currentUser = auth.currentUser ?: return
        try {
            firestore.collection("users").document(currentUser.uid)
                .update("fullName", name).await()
            _fullName.value = name
        } catch (e: Exception) {

        }
    }
}
