package com.nan.nearbystorefinder.domain.model

/**
 * Represents the current state of acquiring the user's real device location.
 * Replaces the previous pattern of silently falling back to a hardcoded
 * sample location (New York) when location was unavailable - every failure
 * mode here is surfaced explicitly so the UI can show a clear state and let
 * the user retry.
 */
sealed class LocationState {
    data object Loading : LocationState()
    data class Available(val location: UserLocation) : LocationState()
    data object PermissionDenied : LocationState()
    data object Unavailable : LocationState()
}
