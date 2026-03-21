package com.nan.nearbystorefinder.presentation.navigation


import androidx.compose.runtime.Composable
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


@Composable
fun AppNavGraph(navController: NavHostController){

    val auth = FirebaseAuth.getInstance()
    val isLoggedIn = auth.currentUser != null

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ){
        composable(route = Screen.Splash.route){
            SplashScreen(
                onSplashFinished = {
                    val nextScreen = if (isLoggedIn) Screen.Home.route else Screen.Login.route
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
                    auth.signOut()
                    navController.navigate(Screen.Login.route){
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }

        composable(route = Screen.Favorite.route){
            FavoriteScreen(navController = navController)
        }

        composable(route = Screen.Profile.route){
            ProfileScreen(navController = navController)
        }

        composable(route = Screen.Login.route){
            LoginScreen(
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
