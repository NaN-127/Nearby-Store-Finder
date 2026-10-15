package com.nan.nearbystorefinder.data.remote.repository

import com.google.android.gms.common.api.ApiException
import com.google.android.libraries.places.api.model.CircularBounds
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.IsOpenRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.api.net.SearchByTextRequest
import com.google.android.libraries.places.api.net.SearchNearbyRequest
import com.google.android.gms.maps.model.LatLng
import com.nan.nearbystorefinder.data.remote.mapper.PlaceMapper
import com.nan.nearbystorefinder.data.remote.photo.PlacesPhotoLoader
import com.nan.nearbystorefinder.domain.model.PlaceCategory
import com.nan.nearbystorefinder.domain.model.PlaceDetailsResult
import com.nan.nearbystorefinder.domain.model.PlacesError
import com.nan.nearbystorefinder.domain.model.PlacesResult
import com.nan.nearbystorefinder.domain.model.Store
import com.nan.nearbystorefinder.domain.repository.StoreRepository
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.tasks.await
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.round

/**
 * [StoreRepository] implementation backed by the Places SDK for Android (New).
 *
 * Implements an in-memory caching and request deduplication layer to eliminate
 * duplicate/repeated network requests caused by recomposition, screen navigation,
 * tab switching, and concurrent calls.
 *
 * Cache keys are scoped by normalized query parameters (including rounded
 * coordinates to absorb GPS jitter) and freshness TTLs. Force-refresh explicitly
 * bypasses the cache to fetch fresh data.
 */
class GooglePlacesStoreRepository(
    private val client: PlacesClient,
    private val photoLoader: PlacesPhotoLoader
) : StoreRepository {

    companion object {
        // 5 minutes freshness for nearby searches & text queries
        const val NEARBY_CACHE_TTL_MS = 5 * 60 * 1000L
        const val SEARCH_CACHE_TTL_MS = 5 * 60 * 1000L
        // 10 minutes freshness for place details
        const val DETAILS_CACHE_TTL_MS = 10 * 60 * 1000L
    }

    private data class CacheEntry<T>(
        val data: T,
        val timestamp: Long = System.currentTimeMillis()
    ) {
        fun isFresh(ttlMs: Long, now: Long = System.currentTimeMillis()): Boolean =
            (now - timestamp) < ttlMs
    }

    internal data class NearbyCacheKey(
        val latRounded: Double,
        val lngRounded: Double,
        val radiusMeters: Int,
        val category: PlaceCategory,
        val sortByDistance: Boolean
    )

    internal data class SearchCacheKey(
        val query: String,
        val latRounded: Double,
        val lngRounded: Double,
        val radiusMeters: Int
    )

    internal data class DetailsCacheKey(
        val placeId: String
    )

    private val nearbyCache = ConcurrentHashMap<NearbyCacheKey, CacheEntry<PlacesResult.Success>>()
    private val searchCache = ConcurrentHashMap<SearchCacheKey, CacheEntry<PlacesResult.Success>>()
    private val detailsCache = ConcurrentHashMap<DetailsCacheKey, CacheEntry<PlaceDetailsResult.Success>>()

    private val requestMutexes = ConcurrentHashMap<Any, Mutex>()

    // Field masks are scoped per-operation and kept as small as possible to avoid
    // unnecessarily expensive fields/SKU tiers, per the "explicit field mask" requirement.
    private val searchFields = listOf(
        Place.Field.ID,
        Place.Field.DISPLAY_NAME,
        Place.Field.LOCATION,
        Place.Field.FORMATTED_ADDRESS,
        Place.Field.TYPES,
        Place.Field.PRIMARY_TYPE,
        Place.Field.RATING,
        Place.Field.USER_RATING_COUNT,
        Place.Field.BUSINESS_STATUS,
        Place.Field.PHOTO_METADATAS
    )

    private val detailsFields = searchFields + listOf(
        Place.Field.OPENING_HOURS,
        Place.Field.CURRENT_OPENING_HOURS,
        Place.Field.UTC_OFFSET,
        Place.Field.PRICE_LEVEL,
        Place.Field.INTERNATIONAL_PHONE_NUMBER,
        Place.Field.WEBSITE_URI,
        Place.Field.GOOGLE_MAPS_URI
    )

    override suspend fun getNearbyStores(
        lat: Double,
        lng: Double,
        radiusMeters: Int,
        category: PlaceCategory,
        maxResults: Int,
        sortByDistance: Boolean,
        forceRefresh: Boolean
    ): PlacesResult {
        // Round to 3 decimal places (~111m) so GPS jitter doesn't miss the cache
        val key = NearbyCacheKey(
            latRounded = round(lat * 1000.0) / 1000.0,
            lngRounded = round(lng * 1000.0) / 1000.0,
            radiusMeters = radiusMeters,
            category = category,
            sortByDistance = sortByDistance
        )

        if (!forceRefresh) {
            val cached = nearbyCache[key]
            if (cached != null && cached.isFresh(NEARBY_CACHE_TTL_MS)) {
                return cached.data
            }
        }

        val mutex = requestMutexes.computeIfAbsent(key) { Mutex() }
        return mutex.withLock {
            if (!forceRefresh) {
                val cached = nearbyCache[key]
                if (cached != null && cached.isFresh(NEARBY_CACHE_TTL_MS)) {
                    return@withLock cached.data
                }
            }

            val result = executeNearbyRequest(lat, lng, radiusMeters, category, maxResults, sortByDistance)
            if (result is PlacesResult.Success) {
                nearbyCache[key] = CacheEntry(result)
            }
            result
        }
    }

    private suspend fun executeNearbyRequest(
        lat: Double,
        lng: Double,
        radiusMeters: Int,
        category: PlaceCategory,
        maxResults: Int,
        sortByDistance: Boolean
    ): PlacesResult {
        return try {
            val circle = CircularBounds.newInstance(LatLng(lat, lng), radiusMeters.toDouble())
            val requestBuilder = SearchNearbyRequest.builder(circle, searchFields)
                .setMaxResultCount(maxResults.coerceIn(1, 20))

            if (category != PlaceCategory.ALL && category.includedTypes.isNotEmpty()) {
                requestBuilder.setIncludedTypes(category.includedTypes)
            }
            // Google Places (New) requires at least one includedType when rankPreference is DISTANCE.
            // If category is ALL (empty includedTypes), setting DISTANCE would fail with an ApiException.
            if (sortByDistance && category != PlaceCategory.ALL && category.includedTypes.isNotEmpty()) {
                requestBuilder.setRankPreference(SearchNearbyRequest.RankPreference.DISTANCE)
            }

            val response = client.searchNearby(requestBuilder.build()).await()
            val mappedStores = mapAndAttachPhotos(response.places, lat, lng)
            val stores = if (sortByDistance) {
                mappedStores.sortedBy { it.distanceKm ?: Double.MAX_VALUE }
            } else {
                mappedStores
            }
            PlacesResult.Success(stores)
        } catch (e: ApiException) {
            PlacesResult.Error(PlacesError.ApiError(e.message))
        } catch (e: IOException) {
            PlacesResult.Error(PlacesError.Network)
        } catch (e: Exception) {
            PlacesResult.Error(PlacesError.Unknown(e))
        }
    }

    override suspend fun searchStores(
        query: String,
        lat: Double,
        lng: Double,
        radiusMeters: Int,
        maxResults: Int,
        forceRefresh: Boolean
    ): PlacesResult {
        // Round to 2 decimal places (~1.1km) for text query caching within the same area
        val normalizedQuery = query.trim().lowercase()
        val key = SearchCacheKey(
            query = normalizedQuery,
            latRounded = round(lat * 100.0) / 100.0,
            lngRounded = round(lng * 100.0) / 100.0,
            radiusMeters = radiusMeters
        )

        if (!forceRefresh) {
            val cached = searchCache[key]
            if (cached != null && cached.isFresh(SEARCH_CACHE_TTL_MS)) {
                return cached.data
            }
        }

        val mutex = requestMutexes.computeIfAbsent(key) { Mutex() }
        return mutex.withLock {
            if (!forceRefresh) {
                val cached = searchCache[key]
                if (cached != null && cached.isFresh(SEARCH_CACHE_TTL_MS)) {
                    return@withLock cached.data
                }
            }

            val result = executeSearchRequest(query, lat, lng, radiusMeters, maxResults)
            if (result is PlacesResult.Success) {
                searchCache[key] = CacheEntry(result)
            }
            result
        }
    }

    private suspend fun executeSearchRequest(
        query: String,
        lat: Double,
        lng: Double,
        radiusMeters: Int,
        maxResults: Int
    ): PlacesResult {
        return try {
            val center = LatLng(lat, lng)
            val requestBuilder = SearchByTextRequest.builder(query, searchFields)
                .setMaxResultCount(maxResults.coerceIn(1, 20))
                .setLocationBias(CircularBounds.newInstance(center, radiusMeters.toDouble()))

            val response = client.searchByText(requestBuilder.build()).await()
            val mappedStores = mapAndAttachPhotos(response.places, lat, lng)
            val stores = mappedStores.sortedBy { it.distanceKm ?: Double.MAX_VALUE }
            PlacesResult.Success(stores)
        } catch (e: ApiException) {
            PlacesResult.Error(PlacesError.ApiError(e.message))
        } catch (e: IOException) {
            PlacesResult.Error(PlacesError.Network)
        } catch (e: Exception) {
            PlacesResult.Error(PlacesError.Unknown(e))
        }
    }

    override suspend fun getPlaceDetails(
        placeId: String,
        userLat: Double?,
        userLng: Double?,
        forceRefresh: Boolean
    ): PlaceDetailsResult {
        val key = DetailsCacheKey(placeId)

        if (!forceRefresh) {
            val cached = detailsCache[key]
            if (cached != null && cached.isFresh(DETAILS_CACHE_TTL_MS)) {
                return cached.data
            }
        }

        val mutex = requestMutexes.computeIfAbsent(key) { Mutex() }
        return mutex.withLock {
            if (!forceRefresh) {
                val cached = detailsCache[key]
                if (cached != null && cached.isFresh(DETAILS_CACHE_TTL_MS)) {
                    return@withLock cached.data
                }
            }

            val result = executeDetailsRequest(placeId, userLat, userLng)
            if (result is PlaceDetailsResult.Success) {
                detailsCache[key] = CacheEntry(result)
            }
            result
        }
    }

    private suspend fun executeDetailsRequest(
        placeId: String,
        userLat: Double?,
        userLng: Double?
    ): PlaceDetailsResult {
        return try {
            val request = FetchPlaceRequest.newInstance(placeId, detailsFields)
            val response = client.fetchPlace(request).await()
            val place = response.place

            val isOpenNow = try {
                client.isOpen(IsOpenRequest.newInstance(place)).await().isOpen
            } catch (e: Exception) {
                null
            }

            var store = PlaceMapper.toStore(place, userLat, userLng, isOpen = isOpenNow)
            val metadata = place.photoMetadatas?.firstOrNull()
            if (metadata != null) {
                store = photoLoader.attachPhotos(
                    listOf(store),
                    mapOf(store.id to metadata),
                    maxWidth = 1200
                ).first()
            }
            PlaceDetailsResult.Success(store)
        } catch (e: ApiException) {
            PlaceDetailsResult.Error(PlacesError.ApiError(e.message))
        } catch (e: IOException) {
            PlaceDetailsResult.Error(PlacesError.Network)
        } catch (e: Exception) {
            PlaceDetailsResult.Error(PlacesError.Unknown(e))
        }
    }

    override fun clearCache() {
        nearbyCache.clear()
        searchCache.clear()
        detailsCache.clear()
        requestMutexes.clear()
        photoLoader.clearCache()
    }

    private suspend fun mapAndAttachPhotos(places: List<Place>, lat: Double, lng: Double): List<Store> {
        if (places.isEmpty()) return emptyList()
        val distinctPlaces = places.distinctBy { it.id }
        val stores = distinctPlaces.map { PlaceMapper.toStore(it, lat, lng) }
        val photoMetadataByPlaceId = distinctPlaces.associate { it.id.orEmpty() to it.photoMetadatas?.firstOrNull() }
        return photoLoader.attachPhotos(stores, photoMetadataByPlaceId)
    }
}
