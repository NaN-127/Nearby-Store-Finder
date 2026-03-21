package com.nan.nearbystorefinder.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FoursquareResponse(
    val results: List<FsqPlaceDto> = emptyList()
)

@Serializable
data class FsqPlaceDto(
    @SerialName("fsq_id") val fsqId: String,
    val categories: List<FsqCategoryDto>? = null,
    val distance: Int? = null,
    val location: FsqLocationDto? = null,
    val name: String,
    val photos: List<FsqPhotoDto>? = null,
    val rating: Float? = null,
    val stats: FsqStatsDto? = null,
    val tel: String? = null,
    val website: String? = null,
    val geocodes: FsqGeocodesDto? = null
)

@Serializable
data class FsqCategoryDto(
    val id: Int,
    val name: String,
    val icon: FsqIconDto? = null
)

@Serializable
data class FsqIconDto(
    val prefix: String,
    val suffix: String
)

@Serializable
data class FsqLocationDto(
    val address: String? = null,
    val country: String? = null,
    @SerialName("cross_street") val crossStreet: String? = null,
    @SerialName("formatted_address") val formattedAddress: String? = null,
    val locality: String? = null,
    val postcode: String? = null,
    val region: String? = null
)

@Serializable
data class FsqGeocodesDto(
    val main: FsqLatLngDto? = null,
    val roof: FsqLatLngDto? = null
)

@Serializable
data class FsqLatLngDto(
    val latitude: Double,
    val longitude: Double
)

@Serializable
data class FsqPhotoDto(
    val id: String,
    @SerialName("created_at") val createdAt: String? = null,
    val prefix: String,
    val suffix: String,
    val width: Int? = null,
    val height: Int? = null
)

@Serializable
data class FsqStatsDto(
    @SerialName("total_photos") val totalPhotos: Int? = null,
    @SerialName("total_ratings") val totalRatings: Int? = null,
    @SerialName("total_tips") val totalTips: Int? = null
)
