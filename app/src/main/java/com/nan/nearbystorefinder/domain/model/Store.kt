package com.nan.nearbystorefinder.domain.model

import kotlinx.serialization.Serializable

/**
 * Domain model representing a single place/store, provider-agnostic.
 *
 * All fields that Google Places does not guarantee to return are nullable or
 * default to an empty value. Nothing here is fabricated: if Google omits a
 * field (e.g. no rating, no photo), the corresponding property stays null
 * rather than being filled with a placeholder/random value.
 */
@Serializable
data class Store(
    val id: String = "",
    val name: String = "",
    val rating: Double? = null,
    val userRatingsTotal: Int? = null,
    /** Distance from the user's current location, in kilometers. Always calculated locally from coordinates. */
    val distanceKm: Double? = null,
    /** Human-readable primary category, e.g. "Restaurant". */
    val category: String = "",
    /** Raw Google place types associated with this place (e.g. ["restaurant", "food", "point_of_interest"]). */
    val types: List<String> = emptyList(),
    val imageUrl: String? = null,
    /** Attribution text that must be displayed alongside the photo, per Google Places policy. */
    val photoAttributions: List<String> = emptyList(),
    val isOpen: Boolean? = null,
    /** e.g. "OPERATIONAL", "CLOSED_TEMPORARILY", "CLOSED_PERMANENTLY". */
    val businessStatus: String? = null,
    val address: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val openingHoursText: List<String> = emptyList(),
    /** 0 (free) to 4 (very expensive), null when Google doesn't report a price level. */
    val priceLevel: Int? = null,
    val phoneNumber: String? = null,
    val websiteUri: String? = null,
    val googleMapsUri: String? = null
)
