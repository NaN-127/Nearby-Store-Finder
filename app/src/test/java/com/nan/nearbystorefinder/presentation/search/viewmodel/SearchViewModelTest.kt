package com.nan.nearbystorefinder.presentation.search.viewmodel

import com.nan.nearbystorefinder.MainDispatcherRule
import com.nan.nearbystorefinder.domain.model.LocationState
import com.nan.nearbystorefinder.domain.model.PlacesError
import com.nan.nearbystorefinder.domain.model.PlacesResult
import com.nan.nearbystorefinder.domain.model.Store
import com.nan.nearbystorefinder.domain.model.UserLocation
import com.nan.nearbystorefinder.fakes.FakeLocationRepository
import com.nan.nearbystorefinder.fakes.FakeStoreRepository
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@kotlinx.coroutines.ExperimentalCoroutinesApi
class SearchViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val fakeStoreRepository = FakeStoreRepository()
    private val fakeLocationRepository = FakeLocationRepository(
        initial = LocationState.Available(UserLocation(1.0, 2.0, "Test location"))
    )
    private val fakeFavoriteDao = com.nan.nearbystorefinder.fakes.FakeFavoriteDao()
    private val fakeFavoriteRepository = com.nan.nearbystorefinder.domain.repository.FavoriteRepository(fakeFavoriteDao)

    private fun viewModel() = SearchViewModel(fakeStoreRepository, fakeLocationRepository, fakeFavoriteRepository)

    @Test
    fun `blank query clears results without calling the repository`() = runTest {
        val vm = viewModel()
        vm.onQueryChange("")
        advanceTimeBy(1000)
        assertTrue(fakeStoreRepository.searchRequests.isEmpty())
        assertFalse(vm.state.value.hasSearched)
    }

    @Test
    fun `successful search populates results`() = runTest {
        val store = Store(id = "1", name = "Test Cafe")
        fakeStoreRepository.searchResult = PlacesResult.Success(listOf(store))

        val vm = viewModel()
        vm.onQueryChange("cafe")
        advanceTimeBy(500) // past the debounce window

        assertEquals(listOf(store), vm.state.value.results)
        assertFalse(vm.state.value.isLoading)
        assertTrue(vm.state.value.hasSearched)
    }

    @Test
    fun `repository error surfaces as an error state, not a crash or silent empty list`() = runTest {
        fakeStoreRepository.searchResult = PlacesResult.Error(PlacesError.Network)

        val vm = viewModel()
        vm.onQueryChange("cafe")
        advanceTimeBy(500)

        assertEquals(PlacesError.Network, vm.state.value.error)
        assertTrue(vm.state.value.results.isEmpty())
    }

    @Test
    fun `search is debounced - rapid keystrokes only trigger one repository call`() = runTest {
        fakeStoreRepository.searchResult = PlacesResult.Success(emptyList())
        val vm = viewModel()

        vm.onQueryChange("c")
        advanceTimeBy(100)
        vm.onQueryChange("ca")
        advanceTimeBy(100)
        vm.onQueryChange("caf")
        advanceTimeBy(500)

        assertEquals(1, fakeStoreRepository.searchRequests.size)
        assertEquals("caf", fakeStoreRepository.searchRequests.first().query)
    }

    @Test
    fun `failed retry preserves previously loaded search results`() = runTest {
        val initialStore = Store(id = "1", name = "Initial Store")
        fakeStoreRepository.searchResult = PlacesResult.Success(listOf(initialStore))
        val vm = viewModel()

        vm.onQueryChange("market")
        advanceTimeBy(500)
        assertEquals(listOf(initialStore), vm.state.value.results)

        // Now a refresh/retry fails with Network error
        fakeStoreRepository.searchResult = PlacesResult.Error(PlacesError.Network)
        vm.retry()
        advanceTimeBy(100)

        // Previous results must be preserved, not wiped!
        assertEquals(listOf(initialStore), vm.state.value.results)
        assertEquals(PlacesError.Network, vm.state.value.error)
    }

    @Test
    fun `no location available does not call the repository`() = runTest {
        val noLocationRepo = FakeLocationRepository(initial = LocationState.Unavailable)
        val vm = SearchViewModel(fakeStoreRepository, noLocationRepo, fakeFavoriteRepository)

        vm.onQueryChange("cafe")
        advanceTimeBy(500)

        assertTrue(fakeStoreRepository.searchRequests.isEmpty())
        assertTrue(vm.state.value.hasSearched)
    }
}
