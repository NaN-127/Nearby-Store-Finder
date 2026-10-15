package com.nan.nearbystorefinder.domain.model

/**
 * UI-facing place category, mapped to valid Google Places (New) type identifiers.
 *
 * [includedTypes] are passed to Nearby Search (New) as `includedTypes` and to
 * Text Search (New) as `includedType` (first entry only, since Text Search
 * only accepts a single type). Values are verified against the official
 * Places API (New) Table A type list.
 *
 * [ALL] applies no type filter at all, letting Google return any nearby place.
 */
enum class PlaceCategory(val label: String, val includedTypes: List<String>) {
    ALL("All", emptyList()),
    RESTAURANTS("Restaurants", listOf("restaurant")),
    CAFES("Cafes", listOf("cafe", "coffee_shop")),
    GROCERY("Grocery", listOf("grocery_store", "supermarket")),
    PHARMACY("Pharmacy", listOf("pharmacy", "drugstore")),
    HOSPITAL("Hospitals", listOf("hospital", "medical_clinic", "doctor")),
    SHOPPING("Shopping", listOf("shopping_mall", "clothing_store", "department_store"));

    companion object {
        fun fromLabel(label: String?): PlaceCategory =
            entries.firstOrNull { it.label == label } ?: ALL
    }
}
