package com.nan.nearbystorefinder.domain.model

/**
 * Result wrapper for place-search operations.
 *
 * Replaces the previous convention of silently swallowing exceptions and
 * returning an empty list, which made it impossible for a ViewModel to tell
 * "no results found" apart from "the request failed". UI layers map
 * [PlacesError] to a dedicated error state with a retry action.
 */
sealed class PlacesResult {
    data class Success(val stores: List<Store>) : PlacesResult()
    data class Error(val error: PlacesError) : PlacesResult()
}

sealed class PlacesError(val message: String) {
    data object Network : PlacesError("Couldn't reach Google Places. Check your connection and try again.")
    data object NoResults : PlacesError("No places found nearby.")
    data class ApiError(val statusMessage: String?) : PlacesError(statusMessage ?: "Google Places returned an error.")
    data class Unknown(val cause: Throwable? = null) : PlacesError("Something went wrong while fetching places.")
}

/** Result wrapper for a single place-details lookup. */
sealed class PlaceDetailsResult {
    data class Success(val store: Store) : PlaceDetailsResult()
    data class Error(val error: PlacesError) : PlaceDetailsResult()
}
