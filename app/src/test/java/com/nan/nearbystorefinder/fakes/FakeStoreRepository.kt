package com.nan.nearbystorefinder.fakes

import com.nan.nearbystorefinder.domain.model.PlaceCategory
import com.nan.nearbystorefinder.domain.model.PlaceDetailsResult
import com.nan.nearbystorefinder.domain.model.PlacesResult
import com.nan.nearbystorefinder.domain.repository.StoreRepository

/**
 * Test double for [StoreRepository]. Never makes a real network call - results
 * are configured directly by the test, and every invocation is recorded so
 * tests can assert on what the ViewModel actually requested (e.g. which
 * category/radius/forceRefresh was passed).
 */
class FakeStoreRepository : StoreRepository {

    var nearbyResult: PlacesResult = PlacesResult.Success(emptyList())
    var searchResult: PlacesResult = PlacesResult.Success(emptyList())
    var detailsResult: PlaceDetailsResult = PlaceDetailsResult.Error(com.nan.nearbystorefinder.domain.model.PlacesError.Unknown())

    val nearbyRequests = mutableListOf<NearbyRequest>()
    val searchRequests = mutableListOf<SearchRequest>()
    val detailsRequests = mutableListOf<DetailsRequest>()
    var clearCacheCallCount = 0

    data class NearbyRequest(
        val lat: Double,
        val lng: Double,
        val radiusMeters: Int,
        val category: PlaceCategory,
        val forceRefresh: Boolean
    )

    data class SearchRequest(
        val query: String,
        val forceRefresh: Boolean
    )

    data class DetailsRequest(
        val placeId: String,
        val forceRefresh: Boolean
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
        nearbyRequests.add(NearbyRequest(lat, lng, radiusMeters, category, forceRefresh))
        return nearbyResult
    }

    override suspend fun searchStores(
        query: String,
        lat: Double,
        lng: Double,
        radiusMeters: Int,
        maxResults: Int,
        forceRefresh: Boolean
    ): PlacesResult {
        searchRequests.add(SearchRequest(query, forceRefresh))
        return searchResult
    }

    override suspend fun getPlaceDetails(
        placeId: String,
        userLat: Double?,
        userLng: Double?,
        forceRefresh: Boolean
    ): PlaceDetailsResult {
        detailsRequests.add(DetailsRequest(placeId, forceRefresh))
        return detailsResult
    }

    override fun clearCache() {
        clearCacheCallCount++
    }
}
