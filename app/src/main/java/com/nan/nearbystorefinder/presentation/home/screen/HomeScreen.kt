package com.nan.nearbystorefinder.presentation.home.screen


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
import androidx.compose.ui.Alignment
import com.nan.nearbystorefinder.R
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
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

    // Use a Box to layer the background behind the entire Scaffold
    Box(modifier = Modifier.fillMaxSize()) {
        // 1. Background Image stays here, ignores Scaffold padding
        Image(
            painter = painterResource(id = R.drawable.map_background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // 2. Gradient Overlay
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

        // 3. The UI Layer
        Scaffold(
            containerColor = Color.Transparent, // Keeps the background visible
            topBar = {
                NearoTopAppBar()
            },
            bottomBar = {
                NearoBottomBar(navController)
            }
        ) { paddingValues ->
            // Only use padding for the content that SHOULD be inset (like lists)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Your Store List or content goes here
            }
        }
    }
}






