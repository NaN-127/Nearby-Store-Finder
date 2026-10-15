package com.nan.nearbystorefinder.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.nan.nearbystorefinder.data.local.database.StringListConverter
import com.nan.nearbystorefinder.domain.model.Store

@Entity(tableName = "favorites")
@TypeConverters(StringListConverter::class)
data class StoreEntity(
    @PrimaryKey val id: String,
    val name: String,
    val rating: Double?,
    val userRatingsTotal: Int?,
    val distanceKm: Double?,
    val category: String,
    val types: List<String>,
    val imageUrl: String?,
    val photoAttributions: List<String>,
    val isOpen: Boolean?,
    val businessStatus: String?,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val openingHoursText: List<String>,
    val priceLevel: Int?,
    val phoneNumber: String?,
    val websiteUri: String?,
    val googleMapsUri: String?
)

fun StoreEntity.toStore(): Store {
    return Store(
        id = id,
        name = name,
        rating = rating,
        userRatingsTotal = userRatingsTotal,
        distanceKm = distanceKm,
        category = category,
        types = types,
        imageUrl = imageUrl,
        photoAttributions = photoAttributions,
        isOpen = isOpen,
        businessStatus = businessStatus,
        address = address,
        latitude = latitude,
        longitude = longitude,
        openingHoursText = openingHoursText,
        priceLevel = priceLevel,
        phoneNumber = phoneNumber,
        websiteUri = websiteUri,
        googleMapsUri = googleMapsUri
    )
}

fun Store.toStoreEntity(): StoreEntity {
    return StoreEntity(
        id = id,
        name = name,
        rating = rating,
        userRatingsTotal = userRatingsTotal,
        distanceKm = distanceKm,
        category = category,
        types = types,
        imageUrl = imageUrl,
        photoAttributions = photoAttributions,
        isOpen = isOpen,
        businessStatus = businessStatus,
        address = address,
        latitude = latitude,
        longitude = longitude,
        openingHoursText = openingHoursText,
        priceLevel = priceLevel,
        phoneNumber = phoneNumber,
        websiteUri = websiteUri,
        googleMapsUri = googleMapsUri
    )
}
