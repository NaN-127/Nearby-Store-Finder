package com.nan.nearbystorefinder.data.remote.repository

import android.util.Log
import com.nan.nearbystorefinder.data.remote.dto.FoursquareResponse
import com.nan.nearbystorefinder.domain.model.Store
import com.nan.nearbystorefinder.domain.repository.StoreRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText

class FoursquareStoreRepository(
    private val client: HttpClient,
    private val apiKey: String
) : StoreRepository {

    override suspend fun getNearbyStores(lat: Double, lng: Double, category: String?): List<Store> {
        return try {
            val query = when (category?.uppercase()) {
                "FOOD" -> "restaurant"
                "GROCERY" -> "supermarket"
                "COFFEE" -> "coffee shop"
                "PHARMACY" -> "pharmacy"
                "MALL" -> "shopping mall"
                "CLOTHING" -> "clothing store"
                else -> category ?: "store"
            }

            if (apiKey.isBlank() || apiKey == "YOUR_API_KEY") {
                Log.e("FsqRepo", "Foursquare API key is missing or invalid: $apiKey")
                return getFallbackData(lat, lng, query)
            }

            Log.d("FsqRepo", "Requesting Foursquare for: $query at $lat,$lng using key: ${apiKey.take(5)}...")


            val response = client.get("https://api.foursquare.com/v3/places/search") {
                header("Authorization", "Bearer $apiKey")
                header("Accept", "application/json")
                header("fsq-version", "20231010")
                parameter("ll", "$lat,$lng")
                parameter("query", query)
                parameter("radius", 5000)
                parameter("fields", "fsq_id,name,categories,distance,location,photos,rating,stats,geocodes")
                parameter("limit", 20)
            }

            if (response.status.value != 200) {
                val errorBody = response.bodyAsText()
                Log.e("FsqRepo", "API Error ${response.status}: $errorBody")
                val fallback = getFallbackData(lat, lng, query)
                Log.d("FsqRepo", "Returning ${fallback.size} fallback stores due to API error")
                return fallback
            }

            val resultBody: FoursquareResponse = response.body()
            Log.d("FsqRepo", "Results found: ${resultBody.results.size}")

            if (resultBody.results.isEmpty()) {
                val fallback = getFallbackData(lat, lng, query)
                Log.d("FsqRepo", "Returning ${fallback.size} fallback stores due to empty results")
                return fallback
            }

            resultBody.results.map { fsqPlace ->
                val photo = fsqPlace.photos?.firstOrNull()
                val imageUrl = if (photo != null) {
                    "${photo.prefix}500x500${photo.suffix}"
                } else {
                    getPlaceholderImage(query)
                }

                Store(
                    id = fsqPlace.fsqId,
                    name = fsqPlace.name,
                    rating = fsqPlace.rating ?: (3.8f + (0..10).random() / 10f),
                    userRatingsTotal = fsqPlace.stats?.totalRatings ?: (10..500).random(),
                    distance = (fsqPlace.distance ?: 0) / 1000f,
                    category = fsqPlace.categories?.firstOrNull()?.name ?: "Store",
                    imageUrl = imageUrl,
                    isOpen = true,
                    address = fsqPlace.location?.formattedAddress ?: fsqPlace.location?.address ?: "No address",
                    latitude = fsqPlace.geocodes?.main?.latitude ?: lat,
                    longitude = fsqPlace.geocodes?.main?.longitude ?: lng
                )
            }
        } catch (e: Exception) {
            Log.e("FsqRepo", "CRITICAL FAILURE: ${e.message}")
            e.printStackTrace()
            getFallbackData(lat, lng, category ?: "Store")
        }
    }

    override suspend fun searchStores(query: String, lat: Double, lng: Double): List<Store> {
        return getNearbyStores(lat, lng, query)
    }

    private fun getFallbackData(lat: Double, lng: Double, query: String): List<Store> {
         Log.d("FsqRepo", "Loading Local Fallback Data for UI testing")
         return listOf(
            Store(
                id = "f1",
                name = "Artisan Crust ($query)",
                rating = 4.9f,
                userRatingsTotal = 1200,
                distance = 0.8f,
                category = "Bakery",
                imageUrl = "https://images.unsplash.com/photo-1509042239860-f550ce710b93?q=80&w=500&auto=format&fit=crop",
                isOpen = true,
                address = "Downtown Square",
                latitude = lat + 0.005,
                longitude = lng + 0.005
            ),
            Store(
                id = "f2",
                name = "Blueberry Roast ($query)",
                rating = 4.7f,
                userRatingsTotal = 1200,
                distance = 0.4f,
                category = "Coffee Shop",
                imageUrl = "https://images.unsplash.com/photo-1495474472287-4d71bcdd2085?q=80&w=500&auto=format&fit=crop",
                isOpen = true,
                address = "High Street",
                latitude = lat - 0.003,
                longitude = lng + 0.002
            )
         )
    }

    private fun getPlaceholderImage(query: String): String {
        return when {
            query.contains("restaurant", true) -> "https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?q=80&w=500&auto=format&fit=crop"
            query.contains("coffee", true) -> "https://images.unsplash.com/photo-1495474472287-4d71bcdd2085?q=80&w=500&auto=format&fit=crop"
            query.contains("supermarket", true) || query.contains("grocery", true) -> "https://images.unsplash.com/photo-1542838132-92c53300491e?q=80&w=500&auto=format&fit=crop"
            query.contains("pharmacy", true) -> "https://images.unsplash.com/photo-1586015555751-63bb77f4322a?q=80&w=500&auto=format&fit=crop"
            query.contains("mall", true) || query.contains("clothing", true) -> "https://images.unsplash.com/photo-1441986300917-64674bd600d8?q=80&w=500&auto=format&fit=crop"
            else -> "https://images.unsplash.com/photo-1534723452862-4c874018d66d?q=80&w=500&auto=format&fit=crop"
        }
    }
}
