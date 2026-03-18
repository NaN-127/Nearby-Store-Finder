package com.nan.nearbystorefinder

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key.Companion.Home
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.nan.nearbystorefinder.presentation.auth.screen.LoginScreen
import com.nan.nearbystorefinder.presentation.navigation.AppNavGraph
import com.nan.nearbystorefinder.ui.theme.NearoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NearoTheme {
                val navController: NavHostController = rememberNavController()
                AppNavGraph(navController = navController)
            }
        }
    }
}


