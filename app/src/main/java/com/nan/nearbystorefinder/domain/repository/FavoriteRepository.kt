package com.nan.nearbystorefinder.domain.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.nan.nearbystorefinder.domain.model.Store
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FavoriteRepository(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    val favorites: Flow<Set<Store>> = callbackFlow {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            android.util.Log.e("FavoriteRepository", "User is null in callbackFlow")
            trySend(emptySet())
            awaitClose { }
            return@callbackFlow
        }

        android.util.Log.d("FavoriteRepository", "Starting snapshot listener for user: ${currentUser.uid}")
        val listener = firestore.collection("users")
            .document(currentUser.uid)
            .collection("favorites")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    android.util.Log.e("FavoriteRepository", "Snapshot error: ${error.message}", error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    android.util.Log.d("FavoriteRepository", "Favorites snapshot received: ${snapshot.size()} items")
                    val storeList = snapshot.documents.mapNotNull { doc ->
                        try {
                            doc.toObject(Store::class.java)
                        } catch (e: Exception) {
                            android.util.Log.e("FavoriteRepository", "Error deserializing store: ${e.message}")
                            null
                        }
                    }.toSet()
                    trySend(storeList)
                }
            }

        awaitClose { 
            android.util.Log.d("FavoriteRepository", "Closing snapshot listener")
            listener.remove() 
        }
    }

    suspend fun toggleFavorite(store: Store) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            android.util.Log.e("FavoriteRepository", "Cannot toggle favorite: user is null")
            return
        }
        val docRef = firestore.collection("users")
            .document(currentUser.uid)
            .collection("favorites")
            .document(store.id)

        try {
            android.util.Log.d("FavoriteRepository", "Toggling favorite for store: ${store.id}")
            val doc = docRef.get().await()
            if (doc.exists()) {
                android.util.Log.d("FavoriteRepository", "Removing store ${store.id} from favorites")
                docRef.delete().await()
            } else {
                android.util.Log.d("FavoriteRepository", "Adding store ${store.id} to favorites")
                docRef.set(store).await()
            }
        } catch (e: Exception) {
            android.util.Log.e("FavoriteRepository", "Error toggling favorite: ${e.message}", e)
        }
    }

    suspend fun removeFavorite(store: Store) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            android.util.Log.e("FavoriteRepository", "Cannot remove favorite: user is null")
            return
        }
        try {
            android.util.Log.d("FavoriteRepository", "Removing favorite for store: ${store.id}")
            firestore.collection("users")
                .document(currentUser.uid)
                .collection("favorites")
                .document(store.id)
                .delete()
                .await()
        } catch (e: Exception) {
            android.util.Log.e("FavoriteRepository", "Error removing favorite: ${e.message}", e)
        }
    }
}
