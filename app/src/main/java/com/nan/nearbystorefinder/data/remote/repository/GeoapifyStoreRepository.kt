package com.nan.nearbystorefinder.data.remote.repository

import android.util.Log.e
import com.nan.nearbystorefinder.data.remote.dto.GeoapifyResponse
import com.nan.nearbystorefinder.domain.model.Store
import com.nan.nearbystorefinder.domain.repository.StoreRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import kotlin.math.roundToInt

class GeoapifyStoreRepository(
    private val client: HttpClient,
    private val apiKey: String
) : StoreRepository {

    override suspend fun getNearbyStores(lat: Double, lng: Double, category: String?): List<Store> {
        return try {
            val categories = when (category?.uppercase()) {
                "FOOD" -> "catering.restaurant,catering.fast_food"
                "GROCERY" -> "commercial.supermarket,commercial.marketplace"
                "COFFEE" -> "catering.cafe"
                "PHARMACY" -> "healthcare.pharmacy"
                "MALL" -> "commercial.shopping_mall"
                "CLOTHING" -> "commercial.clothing"
                else -> "commercial,catering"
            }

            val response: GeoapifyResponse = client.get("https://api.geoapify.com/v2/places") {
                parameter("categories", categories)
                parameter("filter", "circle:$lng,$lat,5000")
                parameter("bias", "proximity:$lng,$lat")
                parameter("limit", 20)
                parameter("apiKey", apiKey)
            }.body()

            response.features.map { feature ->
                val prop = feature.properties
                val distanceInKm = calculateDistance(lat, lng, prop.lat, prop.lon)

                Store(
                    id = prop.placeId,
                    name = prop.name ?: prop.street ?: "Nearby Store",
                    rating = (38 + (0..12).random()).toFloat() / 10f,
                    userRatingsTotal = (50..1500).random(),
                    distance = distanceInKm.toFloat(),
                    category = prop.categories?.firstOrNull()?.split(".")?.lastOrNull()?.replace("_", " ")?.capitalize() ?: "Store",
                    imageUrl = getHighQualityPlaceholderImage(prop.categories?.firstOrNull() ?: "", category ?: ""),
                    isOpen = true,
                    address = prop.addressLine1 ?: prop.addressLine2 ?: "",
                    latitude = prop.lat,
                    longitude = prop.lon
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    override suspend fun searchStores(query: String, lat: Double, lng: Double): List<Store> {
        return getNearbyStores(lat, lng).filter {
            it.name.contains(query, ignoreCase = true)
        }
    }

    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return r * c
    }

    private fun getHighQualityPlaceholderImage(fsqCategory: String, uiCategory: String): String {
        val cat = (fsqCategory + uiCategory).uppercase()
        return when {
            cat.contains("RESTAURANT") || cat.contains("FOOD") -> {
                listOf(
                    "https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?q=80&w=500&auto=format&fit=crop",
                    "https://images.unsplash.com/photo-1552566626-52f8b828add9?q=80&w=500&auto=format&fit=crop",
                    "https://images.unsplash.com/photo-1555396273-367ea4eb4db5?q=80&w=500&auto=format&fit=crop"
                ).random()
            }
            cat.contains("CAFE") || cat.contains("COFFEE") -> {
                listOf(
                    "https://images.unsplash.com/photo-1495474472287-4d71bcdd2085?q=80&w=500&auto=format&fit=crop",
                    "https://images.unsplash.com/photo-1509042239860-f550ce710b93?q=80&w=500&auto=format&fit=crop",
                    "https://images.unsplash.com/photo-1442512595331-e89e73853f31?q=80&w=500&auto=format&fit=crop"
                ).random()
            }
            cat.contains("SUPERMARKET") || cat.contains("GROCERY") -> {
                listOf(
                    "https://images.unsplash.com/photo-1542838132-92c53300491e?q=80&w=500&auto=format&fit=crop",
                    "https://images.unsplash.com/photo-1578916171728-46686eac8d58?q=80&w=500&auto=format&fit=crop",
                    "https://images.unsplash.com/photo-1604719312563-8912e9223c6a?q=80&w=500&auto=format&fit=crop"
                ).random()
            }
            cat.contains("PHARMACY") || cat.contains("HEALTH") -> {
                "https://images.unsplash.com/photo-1586015555751-63bb77f4322a?q=80&w=500&auto=format&fit=crop"
            }
            cat.contains("MALL") || cat.contains("SHOPPING") || cat.contains("CLOTHING") -> {
                listOf(
                    "https://images.unsplash.com/photo-1441986300917-64674bd600d8?q=80&w=500&auto=format&fit=crop",
                    "https://images.unsplash.com/photo-1483985988355-763728e1935b?q=80&w=500&auto=format&fit=crop",
                    "https://images.unsplash.com/photo-1555529669-e69e7aa0ba9a?q=80&w=500&auto=format&fit=crop"
                ).random()
            }
            else -> "https://images.unsplash.com/photo-1534723452862-4c874018d66d?q=80&w=500&auto=format&fit=crop"
        }
    }
}
