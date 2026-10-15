package com.nan.nearbystorefinder.data.remote.mapper

import com.google.android.libraries.places.api.model.Place
import com.nan.nearbystorefinder.domain.model.Store
import com.nan.nearbystorefinder.domain.util.DistanceCalculator

/**
 * Maps the Places SDK's [Place] objects (populated according to the field
 * mask used in the request) into the app's provider-agnostic [Store] domain
 * model. Every field is read defensively since a [Place] only contains the
 * fields that were actually requested/available - nothing is fabricated when
 * Google omits data.
 *
 * Note on "open now": the Places SDK has no plain isOpen property on [Place] -
 * determining it requires a separate [com.google.android.libraries.places.api.net.PlacesClient.isOpen]
 * call. Calling that for every search result would mean one extra request per
 * card, which conflicts with avoiding unnecessary API calls, so [isOpen] is
 * only ever populated by the caller for a single place (the detail screen).
 * Search results instead expose the cheaper [Store.businessStatus] field
 * (operational / closed temporarily / closed permanently).
 */
object PlaceMapper {

    fun toStore(place: Place, userLat: Double?, userLng: Double?, isOpen: Boolean? = null): Store {
        val location = place.location
        val distanceKm = if (location != null && userLat != null && userLng != null) {
            calculateDistanceKm(userLat, userLng, location.latitude, location.longitude)
        } else null

        val primaryType = place.placeTypes?.firstOrNull()
        val category = primaryType?.toDisplayCategory() ?: "Place"

        return Store(
            id = place.id ?: "",
            name = place.displayName ?: place.name ?: "Unnamed place",
            rating = place.rating,
            userRatingsTotal = place.userRatingCount,
            distanceKm = distanceKm,
            category = category,
            types = place.placeTypes ?: emptyList(),
            // imageUrl/photoAttributions are filled in separately by PlacesPhotoLoader,
            // kept out of this mapper so photo fetching stays decoupled from search mapping.
            imageUrl = null,
            photoAttributions = emptyList(),
            isOpen = isOpen,
            businessStatus = place.businessStatus?.name,
            address = place.formattedAddress ?: place.address ?: "",
            latitude = location?.latitude ?: 0.0,
            longitude = location?.longitude ?: 0.0,
            openingHoursText = place.openingHours?.weekdayText ?: emptyList(),
            priceLevel = place.priceLevel,
            phoneNumber = place.internationalPhoneNumber ?: place.nationalPhoneNumber,
            websiteUri = place.websiteUri?.toString(),
            googleMapsUri = place.googleMapsUri?.toString()
        )
    }

    private fun calculateDistanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double =
        DistanceCalculator.distanceKm(lat1, lon1, lat2, lon2)

    private fun String.toDisplayCategory(): String =
        split("_").joinToString(" ") { word -> word.replaceFirstChar { it.uppercase() } }
}
