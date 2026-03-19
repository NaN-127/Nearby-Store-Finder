package com.nan.nearbystorefinder.presentation.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.google.firebase.auth.FirebaseAuth
import com.nan.nearbystorefinder.presentation.auth.screen.LoginScreen
import com.nan.nearbystorefinder.presentation.auth.screen.SignUpScreen
import com.nan.nearbystorefinder.presentation.favorite.screen.FavoriteScreen
import com.nan.nearbystorefinder.presentation.home.screen.HomeScreen
import com.nan.nearbystorefinder.presentation.profile.screen.ProfileScreen
import com.nan.nearbystorefinder.presentation.search.screen.SearchScreen

@Composable
fun AppNavGraph(navController: NavHostController) {

    val isLoggedIn = FirebaseAuth.getInstance().currentUser != null
    val startDestination = if (isLoggedIn) Screen.Home.route else Screen.Login.route

    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = { fadeIn(animationSpec = tween(200)) },
        exitTransition = { fadeOut(animationSpec = tween(200)) },
        popEnterTransition = { fadeIn(animationSpec = tween(200)) },
        popExitTransition = { fadeOut(animationSpec = tween(200)) }
    ) {
        composable(route = Screen.Home.route) {
            HomeScreen(
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                },
                navController = navController
            )
        }

        composable(route = Screen.Login.route) {
            LoginScreen(
                onSignUpClick = {
                    navController.navigate(Screen.SignUp.route)
                },
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(route = Screen.SignUp.route) {
            SignUpScreen(
                onLoginClick = {
                    navController.navigate(Screen.Login.route)
                },
                onSignUpSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.SignUp.route) { inclusive = true }
                    }
                }
            )
        }

        composable(route = Screen.Search.route) {
            SearchScreen(navController)
        }

        composable(route = Screen.Favorite.route) {
            FavoriteScreen(navController)
        }

        composable(route = Screen.Profile.route) {
            ProfileScreen(navController)
        }
    }
}