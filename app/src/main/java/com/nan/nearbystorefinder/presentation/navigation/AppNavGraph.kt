package com.nan.nearbystorefinder.presentation.navigation

import com.nan.nearbystorefinder.presentation.auth.screen.SignUpScreen
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.google.firebase.auth.FirebaseAuth
import com.nan.nearbystorefinder.presentation.auth.screen.LoginScreen
import com.nan.nearbystorefinder.presentation.home.screen.HomeScreen


@Composable
fun AppNavGraph(navController: NavHostController){


    val isLoggedIn = FirebaseAuth.getInstance().currentUser != null

    val startDestination = if (isLoggedIn) Screen.Home.route else Screen.Login.route

    NavHost(
        navController = navController,
        startDestination = startDestination
    ){


        composable(
            route = Screen.Home.route
        ){
            HomeScreen(
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )

        }
        composable(
            route = Screen.Login.route
        ){
            LoginScreen(
                onSignUpClick = {
                    navController.navigate(Screen.SignUp.route)
                },
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route){
                        popUpTo(Screen.Login.route){
                            inclusive = true
                        }
                    }
                }
            )

        }
        composable(
            route = Screen.SignUp.route
        ){
            SignUpScreen(
                onLoginClick = {
                    navController.navigate(Screen.Login.route)
                },

                onSignUpSuccess = {
                    navController.navigate(Screen.Home.route){
                        popUpTo(Screen.SignUp.route){
                            inclusive = true
                        }
                    }
                }
            )

        }
    }

}