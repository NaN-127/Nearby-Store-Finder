package com.nan.nearbystorefinder.presentation.home.screen

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.nan.nearbystorefinder.domain.model.LocationState
import com.nan.nearbystorefinder.presentation.common.EmptyState
import com.nan.nearbystorefinder.presentation.common.ErrorState
import com.nan.nearbystorefinder.presentation.common.PlaceCard
import com.nan.nearbystorefinder.presentation.home.components.CategorySection
import com.nan.nearbystorefinder.presentation.home.components.NearoBottomBar
import com.nan.nearbystorefinder.presentation.home.components.NearoTopAppBar
import com.nan.nearbystorefinder.presentation.home.viewmodel.HomeViewModel
import com.nan.nearbystorefinder.presentation.navigation.Screen
import org.koin.androidx.compose.koinViewModel

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController
) {
    val viewModel: HomeViewModel = koinViewModel()
    val state = viewModel.state
    val favoriteIds by viewModel.favoriteStoreIds.collectAsState()
    val locationState by viewModel.locationState.collectAsState()

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.values.any { it }) {
            viewModel.refreshLocation()
        } else {
            viewModel.onLocationPermissionDenied()
        }
    }

    LaunchedEffect(Unit) {
        locationPermissionLauncher.launch(
            arrayOf(
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    val locationLabel = when (val ls = locationState) {
        is LocationState.Available -> ls.location.address
        is LocationState.Loading -> "Locating..."
        is LocationState.PermissionDenied -> "Location permission required"
        is LocationState.Unavailable -> "Location unavailable"
    }
    val currentLocation = (locationState as? LocationState.Available)?.location

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            NearoTopAppBar(
                location = locationLabel,
                onProfileClick = { navController.navigate(Screen.Profile.route) }
            )
        },
        bottomBar = { NearoBottomBar(navController) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Spacer(Modifier.height(8.dp))
            CategorySection(
                selectedCategory = state.selectedCategory,
                onCategorySelected = { viewModel.onCategorySelected(it) }
            )
            Spacer(Modifier.height(12.dp))

            when (val ls = locationState) {
                is LocationState.PermissionDenied -> ErrorState(
                    message = "Location permission is required to find places near you.",
                    onRetry = {
                        locationPermissionLauncher.launch(
                            arrayOf(
                                android.Manifest.permission.ACCESS_FINE_LOCATION,
                                android.Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    }
                )
                is LocationState.Unavailable -> ErrorState(
                    message = "Could not obtain current device location.",
                    onRetry = { viewModel.refreshLocation() }
                )
                is LocationState.Loading -> LoadingIndicator()
                is LocationState.Available -> {
                    when {
                        state.isLoading -> LoadingIndicator()
                        state.error != null && state.stores.isEmpty() -> ErrorState(
                            message = state.error.message,
                            onRetry = { viewModel.retry() }
                        )
                        state.stores.isEmpty() -> EmptyState(message = "No places found nearby.")
                        else -> {
                            androidx.compose.material3.pulltorefresh.PullToRefreshBox(
                                isRefreshing = state.isRefreshing,
                                onRefresh = { viewModel.refresh() },
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Column(modifier = Modifier.fillMaxSize()) {
                                    Text(
                                        text = "${state.stores.size} places nearby",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                                    )
                                    LazyColumn(
                                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        items(state.stores, key = { it.id }) { store ->
                                            PlaceCard(
                                                store = store,
                                                isFavorite = favoriteIds.contains(store.id),
                                                onFavoriteToggle = { viewModel.toggleFavorite(store) },
                                                onClick = {
                                                    android.util.Log.d("NearoNav", "Navigating to PlaceDetail: ${store.id}")
                                                    navController.navigate(Screen.PlaceDetail.createRoute(store.id))
                                                },
                                                originLat = currentLocation?.latitude,
                                                originLng = currentLocation?.longitude
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LoadingIndicator() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp),
            strokeWidth = 2.dp
        )
    }
}
