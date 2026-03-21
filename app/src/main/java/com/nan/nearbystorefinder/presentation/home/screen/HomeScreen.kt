package com.nan.nearbystorefinder.presentation.home.screen


import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items

import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import com.nan.nearbystorefinder.domain.model.Store
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import com.nan.nearbystorefinder.R
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.nan.nearbystorefinder.presentation.home.components.NearoBottomBar
import com.nan.nearbystorefinder.presentation.home.components.NearoTopAppBar

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import coil.compose.AsyncImage
import com.nan.nearbystorefinder.presentation.home.components.CategorySection
import com.nan.nearbystorefinder.presentation.home.components.NearoBottomBar
import com.nan.nearbystorefinder.presentation.home.components.NearoTopAppBar
import com.nan.nearbystorefinder.presentation.home.viewmodel.HomeViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun HomeScreen(
    onLogout: () -> Unit,
    navController: NavController
){
    val viewModel: HomeViewModel = koinViewModel()
    val state = viewModel.state
    val favoriteIds by viewModel.favoriteStoreIds.collectAsState()
    val context = LocalContext.current

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permission ->
        val granted = permission.values.all { it }
        if(granted){
            viewModel.fetchUserLocation()
        }
    }

    val locationState = viewModel.userLocation
    LaunchedEffect(Unit) {
       locationPermissionLauncher.launch(
           arrayOf(
               android.Manifest.permission.ACCESS_FINE_LOCATION,
               android.Manifest.permission.ACCESS_COARSE_LOCATION
           )
       )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.map_background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF0B0B0F),
                            Color.Transparent
                        )
                    )
                )
        )

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                NearoTopAppBar(
                    location = locationState?.address ?: "Finding location..."
                )
            },
            bottomBar = {
                NearoBottomBar(navController)
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                CategorySection(
                    selectedCategory = state.selectedCategory,
                    onCategorySelected = { viewModel.onCategorySelected(it) }
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (state.isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFF8A7CFF))
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(state.stores, key = { it.id }) { store ->
                            StoreCard(
                                store = store,
                                isFavorite = favoriteIds.contains(store.id),
                                onFavoriteToggle = { viewModel.toggleFavorite(store) },
                                onClick = {
                                    // Navigate to details if needed
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StoreCard(
    store: Store,
    isFavorite: Boolean,
    onFavoriteToggle: () -> Unit,
    onClick: () -> Unit
) {
    android.util.Log.d("HomeScreen", "Rendering StoreCard for ${store.name}, isFavorite: $isFavorite")
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1E))
    ) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().height(180.dp)) {
                AsyncImage(
                    model = store.imageUrl,
                    contentDescription = store.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                
                Surface(
                    modifier = Modifier
                        .padding(12.dp)
                        .align(Alignment.TopEnd)
                        .size(36.dp)
                        .clickable { onFavoriteToggle() },
                    shape = RoundedCornerShape(10.dp),
                    color = Color.Black.copy(alpha = 0.5f)
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Star else Icons.Default.Star,
                        contentDescription = "Favorite",
                        tint = if (isFavorite) Color.Yellow else Color.White,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = store.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFFFB800),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = store.rating.toString(),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "${store.category} • ${String.format("%.1f", store.distance)} km",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = store.address,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.LightGray,
                    maxLines = 1
                )
            }
        }
    }
}






