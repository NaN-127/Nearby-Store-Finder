package com.nan.nearbystorefinder.data.remote.repository

import android.location.Location
import com.nan.nearbystorefinder.data.remote.dto.PlacesResponse
import com.nan.nearbystorefinder.domain.model.Store
import com.nan.nearbystorefinder.domain.repository.StoreRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

class GooglePlacesStoreRepository(
    private val client: HttpClient,
    private val apiKey: String
) : StoreRepository {

    override suspend fun getNearbyStores(lat: Double, lng: Double, category: String?): List<Store> {
        return try {
            val response: PlacesResponse = client.get("https://maps.googleapis.com/maps/api/place/nearbysearch/json") {
                parameter("location", "$lat,$lng")
                parameter("radius", 5000)
                parameter("type", category ?: "store")
                parameter("key", apiKey)
            }.body()

            if (response.status == "OK") {
                response.results.map { it.toStore(lat, lng, apiKey) }
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun searchStores(query: String, lat: Double, lng: Double): List<Store> {
        return try {
            val response: PlacesResponse = client.get("https://maps.googleapis.com/maps/api/place/textsearch/json") {
                parameter("query", query)
                parameter("location", "$lat,$lng")
                parameter("radius", 10000)
                parameter("key", apiKey)
            }.body()

            if (response.status == "OK") {
                response.results.map { it.toStore(lat, lng, apiKey) }
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun com.nan.nearbystorefinder.data.remote.dto.PlaceDto.toStore(userLat: Double, userLng: Double, apiKey: String): Store {
        val resultLocation = Location("").apply {
            latitude = geometry.location.lat
            longitude = geometry.location.lng
        }
        val userLocation = Location("").apply {
            latitude = userLat
            longitude = userLng
        }
        val distanceInKm = userLocation.distanceTo(resultLocation) / 1000

        val photoRef = photos?.firstOrNull()?.photoReference
        val imageUrl = photoRef?.let {
            "https://maps.googleapis.com/maps/api/place/photo?maxwidth=400&photoreference=$it&key=$apiKey"
        }

        return Store(
            id = placeId,
            name = name,
            rating = rating ?: 0f,
            userRatingsTotal = userRatingsTotal ?: 0,
            distance = distanceInKm,
            category = types?.firstOrNull()?.replace("_", " ")?.capitalize() ?: "Store",
            imageUrl = imageUrl,
            isOpen = openingHours?.openNow ?: false,
            address = vicinity ?: "",
            latitude = geometry.location.lat,
            longitude = geometry.location.lng
        )
    }
}
