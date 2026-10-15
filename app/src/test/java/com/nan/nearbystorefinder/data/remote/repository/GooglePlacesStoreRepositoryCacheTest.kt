package com.nan.nearbystorefinder.data.remote.repository

import com.google.android.gms.tasks.Tasks
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceResponse
import com.google.android.libraries.places.api.net.IsOpenResponse
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.api.net.SearchByTextResponse
import com.google.android.libraries.places.api.net.SearchNearbyResponse
import com.nan.nearbystorefinder.data.remote.photo.PlacesPhotoLoader
import com.nan.nearbystorefinder.domain.model.PlaceCategory
import com.nan.nearbystorefinder.domain.model.PlaceDetailsResult
import com.nan.nearbystorefinder.domain.model.PlacesResult
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.lang.reflect.Proxy
import java.util.concurrent.atomic.AtomicInteger

class GooglePlacesStoreRepositoryCacheTest {

    @Test
    fun `test nearby cache hit and force refresh`() = runTest {
        val searchNearbyCallCount = AtomicInteger(0)

        val place = Place.builder()
            .setId("place_1")
            .setName("Test Place")
            .build()
        val mockResponse = SearchNearbyResponse.newInstance(listOf(place))

        val client = Proxy.newProxyInstance(
            PlacesClient::class.java.classLoader,
            arrayOf(PlacesClient::class.java)
        ) { _, method, _ ->
            when (method.name) {
                "searchNearby" -> {
                    searchNearbyCallCount.incrementAndGet()
                    Tasks.forResult(mockResponse)
                }
                "toString" -> "MockPlacesClient"
                "hashCode" -> 12345
                "equals" -> false
                else -> throw UnsupportedOperationException("Method not mocked: ${method.name}")
            }
        } as PlacesClient

        val photoLoader = PlacesPhotoLoader(client)
        val repository = GooglePlacesStoreRepository(client, photoLoader)

        // 1. Initial request: misses cache, executes network call
        val result1 = repository.getNearbyStores(
            lat = 37.422,
            lng = -122.084,
            radiusMeters = 5000,
            category = PlaceCategory.ALL
        )
        assertTrue(result1 is PlacesResult.Success)
        assertEquals(1, searchNearbyCallCount.get())

        // 2. Identical request: hits cache, NO new network call
        val result2 = repository.getNearbyStores(
            lat = 37.422,
            lng = -122.084,
            radiusMeters = 5000,
            category = PlaceCategory.ALL
        )
        assertTrue(result2 is PlacesResult.Success)
        assertEquals(1, searchNearbyCallCount.get())

        // 3. Tiny GPS jitter (< 100m, rounding to 3 decimals): hits cache!
        val resultJitter = repository.getNearbyStores(
            lat = 37.42202,
            lng = -122.08404,
            radiusMeters = 5000,
            category = PlaceCategory.ALL
        )
        assertTrue(resultJitter is PlacesResult.Success)
        assertEquals(1, searchNearbyCallCount.get())

        // 4. Force refresh: bypasses cache and calls PlacesClient
        val result3 = repository.getNearbyStores(
            lat = 37.422,
            lng = -122.084,
            radiusMeters = 5000,
            category = PlaceCategory.ALL,
            forceRefresh = true
        )
        assertTrue(result3 is PlacesResult.Success)
        assertEquals(2, searchNearbyCallCount.get())

        // 5. Clear cache: subsequent request fetches fresh
        repository.clearCache()
        val result4 = repository.getNearbyStores(
            lat = 37.422,
            lng = -122.084,
            radiusMeters = 5000,
            category = PlaceCategory.ALL
        )
        assertTrue(result4 is PlacesResult.Success)
        assertEquals(3, searchNearbyCallCount.get())
    }

    @Test
    fun `test concurrent identical requests are deduplicated to single network call`() = runTest {
        val searchNearbyCallCount = AtomicInteger(0)

        val place = Place.builder()
            .setId("place_1")
            .setName("Test Place")
            .build()
        val mockResponse = SearchNearbyResponse.newInstance(listOf(place))

        val client = Proxy.newProxyInstance(
            PlacesClient::class.java.classLoader,
            arrayOf(PlacesClient::class.java)
        ) { _, method, _ ->
            when (method.name) {
                "searchNearby" -> {
                    searchNearbyCallCount.incrementAndGet()
                    Tasks.forResult(mockResponse)
                }
                "toString" -> "MockPlacesClient"
                "hashCode" -> 12345
                "equals" -> false
                else -> throw UnsupportedOperationException("Method not mocked: ${method.name}")
            }
        } as PlacesClient

        val photoLoader = PlacesPhotoLoader(client)
        val repository = GooglePlacesStoreRepository(client, photoLoader)

        // Launch 5 concurrent requests with identical parameters
        val results = (1..5).map {
            async {
                repository.getNearbyStores(
                    lat = 37.422,
                    lng = -122.084,
                    radiusMeters = 5000,
                    category = PlaceCategory.CAFES
                )
            }
        }.awaitAll()

        // All 5 requests must succeed
        results.forEach { assertTrue(it is PlacesResult.Success) }
        // Exactly ONE network request was performed due to mutex deduplication!
        assertEquals(1, searchNearbyCallCount.get())
    }

    @Test
    fun `test search by text caching and force refresh`() = runTest {
        val searchByTextCallCount = AtomicInteger(0)

        val place = Place.builder()
            .setId("place_2")
            .setName("Bakery")
            .build()
        val mockResponse = SearchByTextResponse.newInstance(listOf(place))

        val client = Proxy.newProxyInstance(
            PlacesClient::class.java.classLoader,
            arrayOf(PlacesClient::class.java)
        ) { _, method, _ ->
            when (method.name) {
                "searchByText" -> {
                    searchByTextCallCount.incrementAndGet()
                    Tasks.forResult(mockResponse)
                }
                "toString" -> "MockPlacesClient"
                "hashCode" -> 12345
                "equals" -> false
                else -> throw UnsupportedOperationException("Method not mocked: ${method.name}")
            }
        } as PlacesClient

        val photoLoader = PlacesPhotoLoader(client)
        val repository = GooglePlacesStoreRepository(client, photoLoader)

        // 1. First search: executes network call
        val result1 = repository.searchStores("bakery", 37.422, -122.084, 5000)
        assertTrue(result1 is PlacesResult.Success)
        assertEquals(1, searchByTextCallCount.get())

        // 2. Same query with whitespace / uppercase variation: hits cache!
        val result2 = repository.searchStores("  Bakery ", 37.422, -122.084, 5000)
        assertTrue(result2 is PlacesResult.Success)
        assertEquals(1, searchByTextCallCount.get())

        // 3. Force refresh: executes network call
        val result3 = repository.searchStores("bakery", 37.422, -122.084, 5000, forceRefresh = true)
        assertTrue(result3 is PlacesResult.Success)
        assertEquals(2, searchByTextCallCount.get())
    }

    @Test
    fun `test place details caching and deduplication`() = runTest {
        val fetchPlaceCallCount = AtomicInteger(0)

        val place = Place.builder()
            .setId("detail_place_1")
            .setName("Detailed Cafe")
            .build()
        val mockResponse = FetchPlaceResponse.newInstance(place)
        val mockIsOpen = IsOpenResponse.newInstance(true)

        val client = Proxy.newProxyInstance(
            PlacesClient::class.java.classLoader,
            arrayOf(PlacesClient::class.java)
        ) { _, method, _ ->
            when (method.name) {
                "fetchPlace" -> {
                    fetchPlaceCallCount.incrementAndGet()
                    Tasks.forResult(mockResponse)
                }
                "isOpen" -> Tasks.forResult(mockIsOpen)
                "toString" -> "MockPlacesClient"
                "hashCode" -> 12345
                "equals" -> false
                else -> throw UnsupportedOperationException("Method not mocked: ${method.name}")
            }
        } as PlacesClient

        val photoLoader = PlacesPhotoLoader(client)
        val repository = GooglePlacesStoreRepository(client, photoLoader)

        // 1. Initial details call
        val result1 = repository.getPlaceDetails("detail_place_1", 37.422, -122.084)
        assertTrue(result1 is PlaceDetailsResult.Success)
        assertEquals(1, fetchPlaceCallCount.get())

        // 2. Re-visiting details (e.g. navigation back and forth): hits cache!
        val result2 = repository.getPlaceDetails("detail_place_1", 37.422, -122.084)
        assertTrue(result2 is PlaceDetailsResult.Success)
        assertEquals(1, fetchPlaceCallCount.get())

        // 3. Force refresh details: fetches fresh data
        val result3 = repository.getPlaceDetails("detail_place_1", 37.422, -122.084, forceRefresh = true)
        assertTrue(result3 is PlaceDetailsResult.Success)
        assertEquals(2, fetchPlaceCallCount.get())
    }
}
