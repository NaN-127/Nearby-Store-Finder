package com.nan.nearbystorefinder.presentation.common

import androidx.annotation.DrawableRes
import com.nan.nearbystorefinder.R

/**
 * Resolves a local category-specific placeholder icon for a place with no
 * photo. Never fetches or fabricates a remote image - this is purely a
 * client-side fallback drawable, keyed off the raw Google place types.
 */
fun categoryPlaceholderIcon(types: List<String>): Int {
    val typeSet = types.toSet()
    return when {
        typeSet.any { it.contains("restaurant") || it == "food" || it == "meal_takeaway" || it == "meal_delivery" } ->
            R.drawable.ic_placeholder_restaurant
        typeSet.any { it.contains("cafe") || it == "coffee_shop" || it == "bakery" } ->
            R.drawable.ic_placeholder_cafe
        typeSet.any { it.contains("grocery") || it == "supermarket" || it == "convenience_store" } ->
            R.drawable.ic_placeholder_grocery
        typeSet.any { it == "pharmacy" || it == "drugstore" } ->
            R.drawable.ic_placeholder_pharmacy
        typeSet.any { it == "hospital" || it.contains("medical") || it == "doctor" || it == "dentist" } ->
            R.drawable.ic_placeholder_hospital
        typeSet.any { it.contains("shopping") || it.contains("store") || it.contains("mall") } ->
            R.drawable.ic_placeholder_shopping
        else -> R.drawable.ic_placeholder_place
    } as Int
}
