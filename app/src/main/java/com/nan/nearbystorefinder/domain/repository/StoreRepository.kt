package com.nan.nearbystorefinder.domain.repository

import com.nan.nearbystorefinder.domain.model.PlaceCategory
import com.nan.nearbystorefinder.domain.model.PlaceDetailsResult
import com.nan.nearbystorefinder.domain.model.PlacesResult

/**
 * Domain-layer contract for nearby-place search. Implementations live in the
 * data layer and are responsible for talking to a concrete provider (Google
 * Places), mapping provider responses into [com.nan.nearbystorefinder.domain.model.Store],
 * and translating failures into [PlacesResult.Error] rather than throwing.
 */
interface StoreRepository {

    /**
     * Searches for places of [category] within [radiusMeters] of ([lat], [lng]).
     * Pass [PlaceCategory.ALL] for an unfiltered search. [sortByDistance] requests
     * distance-ranked results where the provider supports it.
     */
    suspend fun getNearbyStores(
        lat: Double,
        lng: Double,
        radiusMeters: Int,
        category: PlaceCategory,
        maxResults: Int = 20,
        sortByDistance: Boolean = true,
        forceRefresh: Boolean = false
    ): PlacesResult

    /**
     * Free-text search (e.g. "pizza", "24 hour pharmacy") biased around the
     * user's location, for queries that don't map cleanly to a single category.
     */
    suspend fun searchStores(
        query: String,
        lat: Double,
        lng: Double,
        radiusMeters: Int,
        maxResults: Int = 20,
        forceRefresh: Boolean = false
    ): PlacesResult

    /** Full detail lookup for a single place, used by the place detail screen. */
    suspend fun getPlaceDetails(
        placeId: String,
        userLat: Double?,
        userLng: Double?,
        forceRefresh: Boolean = false
    ): PlaceDetailsResult

    /** Clears in-memory cache of nearby, search, and detail places (e.g., on logout). */
    fun clearCache()
}
