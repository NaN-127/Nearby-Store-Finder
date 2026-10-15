package com.nan.nearbystorefinder.presentation.home.viewmodel

import com.nan.nearbystorefinder.MainDispatcherRule
import com.nan.nearbystorefinder.data.local.entity.toStoreEntity
import com.nan.nearbystorefinder.domain.model.LocationState
import com.nan.nearbystorefinder.domain.model.PlaceCategory
import com.nan.nearbystorefinder.domain.model.PlacesError
import com.nan.nearbystorefinder.domain.model.PlacesResult
import com.nan.nearbystorefinder.domain.model.Store
import com.nan.nearbystorefinder.domain.model.UserLocation
import com.nan.nearbystorefinder.domain.repository.FavoriteRepository
import com.nan.nearbystorefinder.fakes.FakeFavoriteDao
import com.nan.nearbystorefinder.fakes.FakeLocationRepository
import com.nan.nearbystorefinder.fakes.FakeStoreRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@kotlinx.coroutines.ExperimentalCoroutinesApi
class HomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val fakeStoreRepository = FakeStoreRepository()
    private val fakeFavoriteDao = FakeFavoriteDao()
    private val favoriteRepository = FavoriteRepository(fakeFavoriteDao)

    private fun viewModel(locationRepository: FakeLocationRepository) =
        HomeViewModel(locationRepository, fakeStoreRepository, favoriteRepository)

    @Test
    fun `never falls back to a hardcoded sample location - search only runs once a real location is available`() = runTest {
        val locationRepository = FakeLocationRepository(initial = LocationState.Loading)
        val vm = viewModel(locationRepository)

        assertTrue(fakeStoreRepository.nearbyRequests.isEmpty())

        locationRepository.emit(LocationState.Available(UserLocation(37.0, -122.0, "Real place")))

        assertEquals(1, fakeStoreRepository.nearbyRequests.size)
        assertEquals(37.0, fakeStoreRepository.nearbyRequests.first().lat, 0.0001)
        assertEquals(-122.0, fakeStoreRepository.nearbyRequests.first().lng, 0.0001)
    }

    @Test
    fun `successful fetch populates stores and clears loading and error`() = runTest {
        val store = Store(id = "1", name = "Place")
        fakeStoreRepository.nearbyResult = PlacesResult.Success(listOf(store))
        val locationRepository = FakeLocationRepository(
            initial = LocationState.Available(UserLocation(1.0, 1.0))
        )

        val vm = viewModel(locationRepository)

        assertEquals(listOf(store), vm.state.stores)
        assertFalse(vm.state.isLoading)
        assertNull(vm.state.error)
    }

    @Test
    fun `repository error is surfaced in state rather than silently becoming an empty list`() = runTest {
        fakeStoreRepository.nearbyResult = PlacesResult.Error(PlacesError.ApiError("boom"))
        val locationRepository = FakeLocationRepository(
            initial = LocationState.Available(UserLocation(1.0, 1.0))
        )

        val vm = viewModel(locationRepository)

        assertTrue(vm.state.error is PlacesError.ApiError)
        assertTrue(vm.state.stores.isEmpty())
    }

    @Test
    fun `selecting a category changes the request's category filter`() = runTest {
        val locationRepository = FakeLocationRepository(
            initial = LocationState.Available(UserLocation(1.0, 1.0))
        )
        val vm = viewModel(locationRepository)

        vm.onCategorySelected(PlaceCategory.CAFES)

        assertEquals(PlaceCategory.CAFES, vm.state.selectedCategory)
        assertEquals(PlaceCategory.CAFES, fakeStoreRepository.nearbyRequests.last().category)
    }

    @Test
    fun `permission denial is reflected in location state without crashing`() = runTest {
        val locationRepository = FakeLocationRepository(initial = LocationState.Loading)
        val vm = viewModel(locationRepository)

        vm.onLocationPermissionDenied()

        assertEquals(LocationState.PermissionDenied, locationRepository.state.value)
    }

    @Test
    fun `toggling favorite delegates to the favorite repository and reflects in favoriteStoreIds`() = runTest {
        val store = Store(id = "abc", name = "Fav Place")
        val locationRepository = FakeLocationRepository(
            initial = LocationState.Available(UserLocation(1.0, 1.0))
        )
        val vm = viewModel(locationRepository)

        vm.toggleFavorite(store)

        assertTrue(fakeFavoriteDao.favoritesState.value.any { it.id == "abc" })
    }

    @Test
    fun `re-emitting identical location or small GPS jitter does not trigger another request`() = runTest {
        val locationRepository = FakeLocationRepository(
            initial = LocationState.Available(UserLocation(1.0, 1.0))
        )
        val vm = viewModel(locationRepository)
        assertEquals(1, fakeStoreRepository.nearbyRequests.size)

        // Same location re-emitted (e.g. screen revisit or lifecycle event)
        locationRepository.emit(LocationState.Available(UserLocation(1.0, 1.0)))
        assertEquals(1, fakeStoreRepository.nearbyRequests.size)

        // Small GPS jitter (< 100 meters, e.g. ~20 meters shift)
        locationRepository.emit(LocationState.Available(UserLocation(1.0001, 1.0001)))
        assertEquals(1, fakeStoreRepository.nearbyRequests.size)
    }

    @Test
    fun `moving to a significantly different location triggers a new request`() = runTest {
        val locationRepository = FakeLocationRepository(
            initial = LocationState.Available(UserLocation(1.0, 1.0))
        )
        val vm = viewModel(locationRepository)
        assertEquals(1, fakeStoreRepository.nearbyRequests.size)

        // Move by ~11km (0.1 degree)
        locationRepository.emit(LocationState.Available(UserLocation(1.1, 1.0)))
        assertEquals(2, fakeStoreRepository.nearbyRequests.size)
    }

    @Test
    fun `re-selecting the already active category does not trigger duplicate request`() = runTest {
        val store = Store(id = "1", name = "Test Cafe")
        fakeStoreRepository.nearbyResult = PlacesResult.Success(listOf(store))
        val locationRepository = FakeLocationRepository(
            initial = LocationState.Available(UserLocation(1.0, 1.0))
        )
        val vm = viewModel(locationRepository)
        assertEquals(1, fakeStoreRepository.nearbyRequests.size)

        // Re-selecting the default category (ALL) when results are already loaded
        vm.onCategorySelected(PlaceCategory.ALL)
        assertEquals(1, fakeStoreRepository.nearbyRequests.size)
    }

    @Test
    fun `pull-to-refresh triggers request with forceRefresh true`() = runTest {
        val store = Store(id = "1", name = "Test Cafe")
        fakeStoreRepository.nearbyResult = PlacesResult.Success(listOf(store))
        val locationRepository = FakeLocationRepository(
            initial = LocationState.Available(UserLocation(1.0, 1.0))
        )
        val vm = viewModel(locationRepository)
        assertEquals(1, fakeStoreRepository.nearbyRequests.size)
        assertFalse(fakeStoreRepository.nearbyRequests.first().forceRefresh)

        vm.refresh()

        assertEquals(2, fakeStoreRepository.nearbyRequests.size)
        assertTrue(fakeStoreRepository.nearbyRequests.last().forceRefresh)
    }

    @Test
    fun `failed refresh preserves previously loaded stores`() = runTest {
        val store = Store(id = "1", name = "Initial Store")
        fakeStoreRepository.nearbyResult = PlacesResult.Success(listOf(store))
        val locationRepository = FakeLocationRepository(
            initial = LocationState.Available(UserLocation(1.0, 1.0))
        )
        val vm = viewModel(locationRepository)
        assertEquals(listOf(store), vm.state.stores)

        // Refresh fails with network error
        fakeStoreRepository.nearbyResult = PlacesResult.Error(PlacesError.Network)
        vm.refresh()

        // Existing stores must be preserved!
        assertEquals(listOf(store), vm.state.stores)
        assertEquals(PlacesError.Network, vm.state.error)
        assertFalse(vm.state.isRefreshing)
    }

    @Test
    fun `stores are sorted ascending by distance to display nearest first`() = runTest {
        val farStore = Store(id = "1", name = "Far Store", distanceKm = 4.5)
        val closeStore = Store(id = "2", name = "Close Store", distanceKm = 0.4)
        val midStore = Store(id = "3", name = "Mid Store", distanceKm = 1.8)
        val unknownDistanceStore = Store(id = "4", name = "Unknown Store", distanceKm = null)

        fakeStoreRepository.nearbyResult = PlacesResult.Success(listOf(farStore, closeStore, midStore, unknownDistanceStore))
        val locationRepository = FakeLocationRepository(
            initial = LocationState.Available(UserLocation(1.0, 1.0))
        )
        val vm = viewModel(locationRepository)

        assertEquals(listOf(closeStore, midStore, farStore, unknownDistanceStore), vm.state.stores)
    }
}
