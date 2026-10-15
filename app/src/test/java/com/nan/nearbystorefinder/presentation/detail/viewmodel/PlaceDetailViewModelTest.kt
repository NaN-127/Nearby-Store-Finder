package com.nan.nearbystorefinder.presentation.detail.viewmodel

import com.nan.nearbystorefinder.MainDispatcherRule
import com.nan.nearbystorefinder.domain.model.LocationState
import com.nan.nearbystorefinder.domain.model.PlaceDetailsResult
import com.nan.nearbystorefinder.domain.model.PlacesError
import com.nan.nearbystorefinder.domain.model.Store
import com.nan.nearbystorefinder.domain.model.UserLocation
import com.nan.nearbystorefinder.domain.repository.FavoriteRepository
import com.nan.nearbystorefinder.fakes.FakeFavoriteDao
import com.nan.nearbystorefinder.fakes.FakeLocationRepository
import com.nan.nearbystorefinder.fakes.FakeStoreRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PlaceDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val fakeStoreRepository = FakeStoreRepository()
    private val fakeFavoriteDao = FakeFavoriteDao()
    private val favoriteRepository = FavoriteRepository(fakeFavoriteDao)
    private val locationRepository = FakeLocationRepository(
        initial = LocationState.Available(UserLocation(37.0, -122.0))
    )

    private fun viewModel(placeId: String = "place_123") =
        PlaceDetailViewModel(placeId, fakeStoreRepository, favoriteRepository, locationRepository)

    @Test
    fun `initial load requests place details with forceRefresh false`() = runTest {
        val store = Store(id = "place_123", name = "Test Store")
        fakeStoreRepository.detailsResult = PlaceDetailsResult.Success(store)

        val vm = viewModel("place_123")

        assertEquals(1, fakeStoreRepository.detailsRequests.size)
        val request = fakeStoreRepository.detailsRequests.first()
        assertEquals("place_123", request.placeId)
        assertFalse(request.forceRefresh)
        assertEquals(store, vm.state.store)
        assertFalse(vm.state.isLoading)
        assertNull(vm.state.error)
    }

    @Test
    fun `refresh triggers details request with forceRefresh true`() = runTest {
        val store = Store(id = "place_123", name = "Test Store")
        fakeStoreRepository.detailsResult = PlaceDetailsResult.Success(store)

        val vm = viewModel("place_123")
        assertEquals(1, fakeStoreRepository.detailsRequests.size)

        vm.refresh()

        assertEquals(2, fakeStoreRepository.detailsRequests.size)
        val lastRequest = fakeStoreRepository.detailsRequests.last()
        assertEquals("place_123", lastRequest.placeId)
        assertTrue(lastRequest.forceRefresh)
    }

    @Test
    fun `error from repository surfaces in state without crashing`() = runTest {
        fakeStoreRepository.detailsResult = PlaceDetailsResult.Error(PlacesError.Network)

        val vm = viewModel("place_123")

        assertNull(vm.state.store)
        assertFalse(vm.state.isLoading)
        assertEquals(PlacesError.Network, vm.state.error)
    }

    @Test
    fun `toggleFavorite updates favorite state and delegates to repository`() = runTest {
        val store = Store(id = "place_123", name = "Test Store")
        fakeStoreRepository.detailsResult = PlaceDetailsResult.Success(store)

        val vm = viewModel("place_123")
        assertFalse(vm.state.isFavorite)

        vm.toggleFavorite()

        assertTrue(vm.state.isFavorite)
        assertTrue(fakeFavoriteDao.favoritesState.value.any { it.id == "place_123" })
    }
}
