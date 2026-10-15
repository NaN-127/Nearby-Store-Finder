package com.nan.nearbystorefinder.domain.repository

import com.nan.nearbystorefinder.domain.model.LocationState
import kotlinx.coroutines.flow.StateFlow

/**
 * Single source of truth for the user's current location, shared across
 * Home, Search, and the place detail screen so location is only requested
 * once rather than independently by every screen that needs it.
 */
interface LocationRepository {
    val state: StateFlow<LocationState>

    /** Attempts to acquire the current location. Safe to call multiple times; a request already in flight is not duplicated. */
    suspend fun refreshLocation(force: Boolean = false)

    /** Called once permission is denied by the user, so the UI can show a permission-denied state instead of spinning forever. */
    fun onPermissionDenied()
}
