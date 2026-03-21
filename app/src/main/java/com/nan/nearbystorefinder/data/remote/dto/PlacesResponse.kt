package com.nan.nearbystorefinder.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PlacesResponse(
    val results: List<PlaceDto>,
    val status: String
)

@Serializable
data class PlaceDto(
    @SerialName("place_id") val placeId: String,
    val name: String,
    val rating: Float? = null,
    @SerialName("user_ratings_total") val userRatingsTotal: Int? = null,
    val types: List<String>? = null,
    val photos: List<PhotoDto>? = null,
    val geometry: GeometryDto,
    @SerialName("opening_hours") val openingHours: OpeningHoursDto? = null,
    val vicinity: String? = null
)

@Serializable
data class PhotoDto(
    @SerialName("photo_reference") val photoReference: String
)

@Serializable
data class GeometryDto(
    val location: LocationDto
)

@Serializable
data class LocationDto(
    val lat: Double,
    val lng: Double
)

@Serializable
data class OpeningHoursDto(
    @SerialName("open_now") val openNow: Boolean? = null
)
