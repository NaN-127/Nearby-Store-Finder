package com.nan.nearbystorefinder.data.local.repository

import com.nan.nearbystorefinder.core.location.LocationClient
import com.nan.nearbystorefinder.domain.model.LocationState
import com.nan.nearbystorefinder.domain.repository.LocationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

import com.nan.nearbystorefinder.core.location.ILocationClient

class LocationRepositoryImpl(
    private val locationClient: ILocationClient
) : LocationRepository {

    companion object {
        // Location is considered fresh for 2 minutes unless explicitly forced
        const val LOCATION_FRESHNESS_MS = 2 * 60 * 1000L
        // Movement under 50m is treated as GPS jitter and avoids re-geocoding
        const val JITTER_THRESHOLD_METERS = 50.0
    }

    private val _state = MutableStateFlow<LocationState>(LocationState.Loading)
    override val state: StateFlow<LocationState> = _state.asStateFlow()

    // Guards against overlapping refresh calls from firing duplicate location/geocoding requests
    private val mutex = Mutex()
    private var lastAcquiredTimeMs = 0L

    override suspend fun refreshLocation(force: Boolean) {
        val currentState = _state.value
        val now = System.currentTimeMillis()

        // If location is already available and fresh, and this isn't a forced refresh, return immediately
        if (!force && currentState is LocationState.Available && (now - lastAcquiredTimeMs) < LOCATION_FRESHNESS_MS) {
            return
        }

        mutex.withLock {
            // Double-check inside lock: another coroutine might have just refreshed it
            val stateInsideLock = _state.value
            val nowInsideLock = System.currentTimeMillis()
            if (!force && stateInsideLock is LocationState.Available && (nowInsideLock - lastAcquiredTimeMs) < LOCATION_FRESHNESS_MS) {
                return@withLock
            }

            // Only show Loading if we don't already have an available location to display
            if (stateInsideLock !is LocationState.Available) {
                _state.value = LocationState.Loading
            }

            val coords = locationClient.getCurrentLocation()
            if (coords != null) {
                val previousAvailable = stateInsideLock as? LocationState.Available
                val address = if (previousAvailable != null &&
                    distanceMeters(previousAvailable.location.latitude, previousAvailable.location.longitude, coords.latitude, coords.longitude) < JITTER_THRESHOLD_METERS &&
                    previousAvailable.location.address.isNotBlank() &&
                    previousAvailable.location.address != "Location Unavailable"
                ) {
                    previousAvailable.location.address
                } else {
                    locationClient.getAddressFromCoords(coords.latitude, coords.longitude)
                }

                lastAcquiredTimeMs = System.currentTimeMillis()
                _state.value = LocationState.Available(coords.copy(address = address))
            } else {
                // If GPS acquisition failed but we already had an available location, preserve it
                if (stateInsideLock !is LocationState.Available) {
                    _state.value = LocationState.Unavailable
                }
            }
        }
    }

    override fun onPermissionDenied() {
        _state.value = LocationState.PermissionDenied
    }

    internal fun distanceMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        return com.nan.nearbystorefinder.domain.util.DistanceCalculator.distanceKm(lat1, lon1, lat2, lon2) * 1000.0
    }
}
