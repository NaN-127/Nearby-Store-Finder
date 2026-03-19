package com.nan.nearbystorefinder.presentation.favorite.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.nan.nearbystorefinder.presentation.home.components.NearoBottomBar


@Composable
fun FavoriteScreen(navController: NavController){
    Scaffold(
        bottomBar = { NearoBottomBar(navController) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Text("Favorite Screen")
        }
    }
}