package com.nan.nearbystorefinder.presentation.navigation


import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.google.firebase.auth.FirebaseAuth
import com.nan.nearbystorefinder.presentation.auth.screen.LoginScreen
import com.nan.nearbystorefinder.presentation.auth.screen.SignUpScreen
import com.nan.nearbystorefinder.presentation.home.screen.HomeScreen
import com.nan.nearbystorefinder.presentation.profile.screen.ProfileScreen
import com.nan.nearbystorefinder.presentation.favorite.screen.FavoriteScreen
import com.nan.nearbystorefinder.presentation.splash.SplashScreen
import com.nan.nearbystorefinder.presentation.auth.viewmodel.AuthViewModel
import org.koin.androidx.compose.koinViewModel


@Composable
fun AppNavGraph(navController: NavHostController){

    val authViewModel: AuthViewModel = koinViewModel()
    val authState = authViewModel.state

    LaunchedEffect(authState.user) {
        if (authState.user == null) {
            if (navController.currentDestination?.route != Screen.Login.route) {
                navController.navigate(Screen.Login.route) {
                    popUpTo(0)
                }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ){
        composable(route = Screen.Splash.route){
            SplashScreen(
                onSplashFinished = {
                    val nextScreen = if (authState.user != null) Screen.Home.route else Screen.Login.route
                    navController.navigate(nextScreen){
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(route = Screen.Home.route){
            HomeScreen(
                navController = navController,
                onLogout = {
                    authViewModel.logout()
                }
            )
        }

        composable(route = Screen.Favorite.route){
            FavoriteScreen(
                navController = navController,
                onLogout = { authViewModel.logout() }
            )
        }

        composable(route = Screen.Profile.route){
            ProfileScreen(
                navController = navController,
                authViewModel = authViewModel,
                onLogout = { authViewModel.logout() }
            )
        }

        composable(route = Screen.Login.route){
            LoginScreen(
                authViewModel = authViewModel,
                onSignUpClick = {
                    navController.navigate(Screen.SignUp.route)
                },
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route){
                        popUpTo(Screen.Login.route){ inclusive = true }
                    }
                }
            )
        }

        composable(route = Screen.SignUp.route){
            SignUpScreen(
                authViewModel = authViewModel,
                onLoginClick = {
                    navController.navigate(Screen.Login.route)
                },
                onSignUpSuccess = {
                    navController.navigate(Screen.Home.route){
                        popUpTo(Screen.SignUp.route){ inclusive = true }
                    }
                }
            )
        }
    }
}
