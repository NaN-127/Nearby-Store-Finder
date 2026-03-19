package com.nan.nearbystorefinder.presentation.navigation


sealed class Screen(val route: String){
    object Login: Screen("login")
    object SignUp: Screen("signup")
    object Home: Screen("home")

    object Search: Screen("search")

    object Favorite: Screen("like")

    object Profile: Screen("profile")
}

