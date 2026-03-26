package com.nan.nearbystorefinder.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.nan.nearbystorefinder.domain.model.Store

@Entity(tableName = "favorites")
data class StoreEntity(
    @PrimaryKey val id: String,
    val name: String,
    val rating: Double,
    val userRatingsTotal: Int,
    val distance: Double,
    val category: String,
    val imageUrl: String?,
    val isOpen: Boolean,
    val address: String,
    val latitude: Double,
    val longitude: Double
)

fun StoreEntity.toStore(): Store {
    return Store(
        id = id,
        name = name,
        rating = rating,
        userRatingsTotal = userRatingsTotal,
        distance = distance,
        category = category,
        imageUrl = imageUrl,
        isOpen = isOpen,
        address = address,
        latitude = latitude,
        longitude = longitude
    )
}

fun Store.toStoreEntity(): StoreEntity {
    return StoreEntity(
        id = id,
        name = name,
        rating = rating,
        userRatingsTotal = userRatingsTotal,
        distance = distance,
        category = category,
        imageUrl = imageUrl,
        isOpen = isOpen,
        address = address,
        latitude = latitude,
        longitude = longitude

    )
}
