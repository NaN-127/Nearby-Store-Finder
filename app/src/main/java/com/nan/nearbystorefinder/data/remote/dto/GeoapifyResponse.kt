package com.nan.nearbystorefinder.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GeoapifyResponse(
    val features: List<FeatureDto>
)

@Serializable
data class FeatureDto(
    val properties: PropertyDto,
    val geometry: GeoGeometryDto
)

@Serializable
data class PropertyDto(
    val name: String? = null,
    val country: String? = null,
    val city: String? = null,
    val street: String? = null,
    @SerialName("address_line1") val addressLine1: String? = null,
    @SerialName("address_line2") val addressLine2: String? = null,
    val categories: List<String>? = null,
    @SerialName("place_id") val placeId: String,
    val lat: Double,
    val lon: Double
)

@Serializable
data class GeoGeometryDto(
    val type: String,
    val coordinates: List<Double>
)
