package com.nan.nearbystorefinder.data.remote.photo

import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.PhotoMetadata
import com.google.android.libraries.places.api.net.FetchResolvedPhotoUriRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.nan.nearbystorefinder.domain.model.Store
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await
import java.util.concurrent.ConcurrentHashMap

/**
 * Resolves Place Photos (New) URIs separately from search/detail mapping, so
 * photo-fetching concerns never leak into [com.nan.nearbystorefinder.data.remote.repository.GooglePlacesStoreRepository]'s
 * request-building and DTO-mapping logic.
 *
 * Resolved photo URIs and attributions are cached in-memory by place ID to prevent
 * duplicate Place Photos SKU billing when places are revisited, re-rendered, or filtered.
 *
 * Only the first photo per place is resolved (one Place Photos SKU call per
 * place) to avoid unnecessary billed requests. Attribution strings returned
 * by Google are attached to the Store so the UI can display them, per
 * Places API photo policy.
 */
class PlacesPhotoLoader(
    private val client: PlacesClient
) {
    private val photoCache = ConcurrentHashMap<String, Pair<String?, List<String>>>()

    /** Resolves photos for a list of places that already carry [PhotoMetadata] via [Place.getPhotoMetadatas], concurrently. */
    suspend fun attachPhotos(stores: List<Store>, photoMetadatas: Map<String, PhotoMetadata?>, maxWidth: Int = 800): List<Store> =
        coroutineScope {
            stores.map { store ->
                async {
                    val cached = photoCache[store.id]
                    if (cached != null) {
                        return@async store.copy(imageUrl = cached.first, photoAttributions = cached.second)
                    }

                    val metadata = photoMetadatas[store.id] ?: return@async store
                    val resolved = resolvePhoto(store, metadata, maxWidth)
                    photoCache[store.id] = Pair(resolved.imageUrl, resolved.photoAttributions)
                    resolved
                }
            }.map { it.await() }
        }

    private suspend fun resolvePhoto(store: Store, metadata: PhotoMetadata, maxWidth: Int): Store {
        return try {
            val request = FetchResolvedPhotoUriRequest.builder(metadata)
                .setMaxWidth(maxWidth)
                .build()
            val response = client.fetchResolvedPhotoUri(request).await()
            val attributions = metadata.attributions?.let { listOf(it) } ?: emptyList()
            store.copy(imageUrl = response.uri?.toString(), photoAttributions = attributions)
        } catch (e: Exception) {
            // No photo available or the fetch failed - leave imageUrl null so the UI
            // falls back to a category placeholder rather than crashing or retrying.
            store
        }
    }

    fun clearCache() {
        photoCache.clear()
    }
}
