package com.nan.nearbystorefinder.fakes

import com.nan.nearbystorefinder.domain.model.LocationState
import com.nan.nearbystorefinder.domain.repository.LocationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FakeLocationRepository(initial: LocationState = LocationState.Loading) : LocationRepository {
    private val _state = MutableStateFlow(initial)
    override val state: StateFlow<LocationState> = _state

    var refreshCount = 0
    var forceRefreshCount = 0

    override suspend fun refreshLocation(force: Boolean) {
        refreshCount++
        if (force) {
            forceRefreshCount++
        }
    }

    override fun onPermissionDenied() {
        _state.value = LocationState.PermissionDenied
    }

    fun emit(newState: LocationState) {
        _state.value = newState
    }
}
