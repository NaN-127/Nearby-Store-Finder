package com.nan.nearbystorefinder.presentation.favorite.screen

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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.nan.nearbystorefinder.presentation.common.EmptyState
import com.nan.nearbystorefinder.presentation.common.PlaceCard
import com.nan.nearbystorefinder.presentation.favorite.viewmodel.FavoriteViewModel
import com.nan.nearbystorefinder.presentation.home.components.NearoBottomBar
import com.nan.nearbystorefinder.presentation.navigation.Screen
import org.koin.androidx.compose.koinViewModel

@Composable
fun FavoriteScreen(navController: NavController) {
    val viewModel: FavoriteViewModel = koinViewModel()
    val state = viewModel.state

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = { NearoBottomBar(navController) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                Text(
                    text = "Favorites",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "${state.favorites.size} saved places",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            when {
                state.isLoading -> Box(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                }
                state.favorites.isEmpty() -> {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        EmptyState(message = "No saved places yet.")
                        Spacer(Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = { navController.navigate(Screen.Home.route) },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.onBackground
                            )
                        ) {
                            Text(text = "Explore Nearby Places", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
                else -> LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.favorites, key = { it.id }) { store ->
                        PlaceCard(
                            store = store,
                            isFavorite = true,
                            onFavoriteToggle = { viewModel.removeFavorite(store) },
                            onClick = { navController.navigate(Screen.PlaceDetail.createRoute(store.id)) }
                        )
                    }
                }
            }
        }
    }
}
