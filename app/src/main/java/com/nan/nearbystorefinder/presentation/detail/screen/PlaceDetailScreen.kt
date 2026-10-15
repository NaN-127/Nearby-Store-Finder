package com.nan.nearbystorefinder.presentation.detail.screen

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.nan.nearbystorefinder.core.maps.MapsNavigator
import com.nan.nearbystorefinder.domain.model.LocationState
import com.nan.nearbystorefinder.domain.model.Store
import com.nan.nearbystorefinder.domain.util.UrlFormatter
import com.nan.nearbystorefinder.presentation.common.ErrorState
import com.nan.nearbystorefinder.presentation.common.categoryPlaceholderIcon
import com.nan.nearbystorefinder.presentation.detail.viewmodel.PlaceDetailViewModel
import com.nan.nearbystorefinder.ui.theme.StatusClosed
import com.nan.nearbystorefinder.ui.theme.StatusOpen
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun PlaceDetailScreen(
    placeId: String,
    navController: NavController
) {
    val viewModel: PlaceDetailViewModel = koinViewModel(parameters = { parametersOf(placeId) })
    val state = viewModel.state
    val locationState by viewModel.locationState.collectAsState()
    val context = LocalContext.current

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.navigateUp() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                state.isLoading -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp),
                        strokeWidth = 2.dp
                    )
                }
                state.error != null -> ErrorState(
                    message = state.error.message,
                    onRetry = { viewModel.retry() }
                )
                state.store != null -> {
                    val origin = (locationState as? LocationState.Available)?.location
                    PlaceDetailContent(
                        store = state.store,
                        isFavorite = state.isFavorite,
                        onFavoriteToggle = { viewModel.toggleFavorite() },
                        onDirectionsClick = {
                            MapsNavigator.openDirections(
                                context = context,
                                destinationLat = state.store.latitude,
                                destinationLng = state.store.longitude,
                                destinationPlaceId = state.store.id,
                                originLat = origin?.latitude,
                                originLng = origin?.longitude
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun PlaceDetailContent(
    store: Store,
    isFavorite: Boolean,
    onFavoriteToggle: () -> Unit,
    onDirectionsClick: () -> Unit
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Hero Image
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            if (store.imageUrl != null) {
                AsyncImage(
                    model = store.imageUrl,
                    contentDescription = store.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = categoryPlaceholderIcon(store.types)),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Title & Favorite
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                Text(
                    text = store.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                if (store.category.isNotBlank()) {
                    Text(
                        text = store.category,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            IconButton(onClick = onFavoriteToggle) {
                Icon(
                    imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (isFavorite) StatusClosed else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // Rating & Status
        Row(
            modifier = Modifier.padding(top = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (store.rating != null) {
                Icon(
                    Icons.Default.Star,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = "%.1f".format(store.rating) + (store.userRatingsTotal?.let { " ($it reviews)" } ?: ""),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(Modifier.width(10.dp))
            }

            val (statusText, statusColor) = when {
                store.isOpen == true -> "Open Now" to StatusOpen
                store.isOpen == false -> "Closed" to StatusClosed
                else -> null to null
            }
            if (statusText != null && statusColor != null) {
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = statusColor
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        // Primary Action: Get Directions
        Button(
            onClick = onDirectionsClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Default.Directions,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Get Directions",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        // Info Cards (Address, Phone, Website)
        if (store.address.isNotBlank()) {
            DetailInfoCard(
                label = "Address",
                value = store.address
            )
            Spacer(Modifier.height(10.dp))
        }

        if (!store.phoneNumber.isNullOrBlank()) {
            DetailActionCard(
                label = "Phone",
                value = store.phoneNumber,
                icon = Icons.Default.Phone,
                onClick = {
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${store.phoneNumber}"))
                    context.startActivity(intent)
                }
            )
            Spacer(Modifier.height(10.dp))
        }

        if (!store.websiteUri.isNullOrBlank()) {
            val displayUrl = UrlFormatter.formatDisplayUrl(store.websiteUri)
            DetailActionCard(
                label = "Website",
                value = displayUrl,
                hint = "Opens in browser",
                icon = Icons.AutoMirrored.Outlined.OpenInNew,
                onClick = {
                    try {
                        val safeUrl = if (!store.websiteUri.startsWith("http://", ignoreCase = true) &&
                            !store.websiteUri.startsWith("https://", ignoreCase = true)
                        ) {
                            "https://${store.websiteUri}"
                        } else {
                            store.websiteUri
                        }
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(safeUrl))
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        android.util.Log.e("PlaceDetail", "Unable to open website: ${store.websiteUri}", e)
                    }
                }
            )
            Spacer(Modifier.height(10.dp))
        }

        if (store.openingHoursText.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Opening Hours",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(8.dp))
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    for (desc in store.openingHoursText) {
                        Text(
                            text = desc,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun DetailInfoCard(label: String, value: String) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(3.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun DetailActionCard(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    hint: String? = null,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (hint != null) {
                        Text(
                            text = " • $hint",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                        )
                    }
                }
                Spacer(Modifier.height(3.dp))
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(Modifier.width(8.dp))
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
