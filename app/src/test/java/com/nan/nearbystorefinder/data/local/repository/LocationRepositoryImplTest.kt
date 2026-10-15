package com.nan.nearbystorefinder.data.local.repository

import com.nan.nearbystorefinder.core.location.ILocationClient
import com.nan.nearbystorefinder.domain.model.LocationState
import com.nan.nearbystorefinder.domain.model.UserLocation
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LocationRepositoryImplTest {

    private class FakeTestLocationClient : ILocationClient {
        var locationToReturn: UserLocation? = UserLocation(37.422, -122.084)
        var addressToReturn: String = "Mountain View, CA"
        var getLocationCallCount = 0
        var getAddressCallCount = 0

        override suspend fun getCurrentLocation(): UserLocation? {
            getLocationCallCount++
            return locationToReturn
        }

        override suspend fun getAddressFromCoords(lat: Double, long: Double): String {
            getAddressCallCount++
            return addressToReturn
        }
    }

    @Test
    fun `refreshLocation acquires coords and geocodes address`() = runTest {
        val client = FakeTestLocationClient()
        val repository = LocationRepositoryImpl(client)

        repository.refreshLocation()

        val state = repository.state.value
        assertTrue(state is LocationState.Available)
        val available = state as LocationState.Available
        assertEquals(37.422, available.location.latitude, 0.0001)
        assertEquals(-122.084, available.location.longitude, 0.0001)
        assertEquals("Mountain View, CA", available.location.address)
        assertEquals(1, client.getLocationCallCount)
        assertEquals(1, client.getAddressCallCount)
    }

    @Test
    fun `consecutive refreshLocation calls within freshness window do not query GPS again`() = runTest {
        val client = FakeTestLocationClient()
        val repository = LocationRepositoryImpl(client)

        repository.refreshLocation()
        assertEquals(1, client.getLocationCallCount)

        // Second call without force within 2 minutes: should be a no-op
        repository.refreshLocation(force = false)
        assertEquals(1, client.getLocationCallCount)
        assertEquals(1, client.getAddressCallCount)
    }

    @Test
    fun `refreshLocation with force true forces new acquisition`() = runTest {
        val client = FakeTestLocationClient()
        val repository = LocationRepositoryImpl(client)

        repository.refreshLocation()
        assertEquals(1, client.getLocationCallCount)

        repository.refreshLocation(force = true)
        assertEquals(2, client.getLocationCallCount)
    }

    @Test
    fun `GPS jitter under 50m reuses existing address without hitting geocoder`() = runTest {
        val client = FakeTestLocationClient()
        val repository = LocationRepositoryImpl(client)

        // First fix at (37.422000, -122.084000)
        client.locationToReturn = UserLocation(37.422000, -122.084000)
        repository.refreshLocation()
        assertEquals(1, client.getAddressCallCount)

        // Second fix with slight jitter (~15 meters away)
        client.locationToReturn = UserLocation(37.422100, -122.084100)
        repository.refreshLocation(force = true)

        assertEquals(2, client.getLocationCallCount)
        // Address geocoder must NOT have been called a second time because movement < 50m
        assertEquals(1, client.getAddressCallCount)
        val state = repository.state.value as LocationState.Available
        assertEquals("Mountain View, CA", state.location.address)
    }

    @Test
    fun `movement over 50m calls geocoder for fresh address`() = runTest {
        val client = FakeTestLocationClient()
        val repository = LocationRepositoryImpl(client)

        client.locationToReturn = UserLocation(37.422000, -122.084000)
        repository.refreshLocation()
        assertEquals(1, client.getAddressCallCount)

        // Move to San Jose (~20 km away)
        client.locationToReturn = UserLocation(37.3382, -121.8863)
        client.addressToReturn = "San Jose, CA"
        repository.refreshLocation(force = true)

        assertEquals(2, client.getLocationCallCount)
        assertEquals(2, client.getAddressCallCount)
        val state = repository.state.value as LocationState.Available
        assertEquals("San Jose, CA", state.location.address)
    }

    @Test
    fun `onPermissionDenied updates state to PermissionDenied`() {
        val client = FakeTestLocationClient()
        val repository = LocationRepositoryImpl(client)

        repository.onPermissionDenied()

        assertEquals(LocationState.PermissionDenied, repository.state.value)
    }
}
