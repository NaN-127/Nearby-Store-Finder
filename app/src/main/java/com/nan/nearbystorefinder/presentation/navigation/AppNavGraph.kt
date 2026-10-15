package com.nan.nearbystorefinder.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.nan.nearbystorefinder.presentation.auth.screen.LoginScreen
import com.nan.nearbystorefinder.presentation.auth.screen.SignUpScreen
import com.nan.nearbystorefinder.presentation.auth.viewmodel.AuthViewModel
import com.nan.nearbystorefinder.presentation.detail.screen.PlaceDetailScreen
import com.nan.nearbystorefinder.presentation.favorite.screen.FavoriteScreen
import com.nan.nearbystorefinder.presentation.home.screen.HomeScreen
import com.nan.nearbystorefinder.presentation.profile.screen.ProfileScreen
import com.nan.nearbystorefinder.presentation.search.screen.SearchScreen
import com.nan.nearbystorefinder.presentation.splash.SplashScreen
import org.koin.androidx.compose.koinViewModel

@Composable
fun AppNavGraph(navController: NavHostController) {
    val authViewModel: AuthViewModel = koinViewModel()
    val authState = authViewModel.state

    LaunchedEffect(authState.user, authState.isGuest) {
        val currentRoute = navController.currentDestination?.route
        val isAuthRoute = currentRoute == Screen.Login.route ||
                currentRoute == Screen.SignUp.route ||
                currentRoute == Screen.Splash.route

        if (authState.user == null && !authState.isGuest) {
            if (!isAuthRoute && currentRoute != null) {
                navController.navigate(Screen.Login.route) {
                    popUpTo(0)
                }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(route = Screen.Splash.route) {
            SplashScreen(
                onSplashFinished = {
                    val nextScreen = if (authState.user != null || authState.isGuest) {
                        Screen.Home.route
                    } else {
                        Screen.Login.route
                    }
                    navController.navigate(nextScreen) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(route = Screen.Home.route) {
            HomeScreen(navController = navController)
        }

        composable(route = Screen.Search.route) {
            SearchScreen(navController = navController)
        }

        composable(route = Screen.Favorite.route) {
            FavoriteScreen(navController = navController)
        }

        composable(route = Screen.Profile.route) {
            ProfileScreen(
                navController = navController,
                userEmail = authState.user?.email,
                userDisplayName = authState.user?.displayName,
                isGuest = authState.isGuest,
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0)
                    }
                }
            )
        }

        composable(
            route = Screen.PlaceDetail.route,
            arguments = listOf(navArgument(Screen.PlaceDetail.ARG_PLACE_ID) { type = NavType.StringType })
        ) { backStackEntry ->
            val placeId = backStackEntry.arguments?.getString(Screen.PlaceDetail.ARG_PLACE_ID).orEmpty()
            PlaceDetailScreen(placeId = placeId, navController = navController)
        }

        composable(route = Screen.Login.route) {
            LoginScreen(
                authViewModel = authViewModel,
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
                authViewModel = authViewModel,
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
    }
}
