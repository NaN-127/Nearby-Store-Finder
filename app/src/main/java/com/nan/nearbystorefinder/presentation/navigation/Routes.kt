package com.nan.nearbystorefinder.presentation.navigation

sealed class Screen(val route: String){
    object Splash: Screen("splash")
    object Login: Screen("login")
    object SignUp: Screen("signup")
    object Home: Screen("home")
    object Search: Screen("search")
    object Favorite: Screen("like")
    object Profile: Screen("profile")

    object PlaceDetail: Screen("place/{placeId}") {
        fun createRoute(placeId: String) = "place/$placeId"
        const val ARG_PLACE_ID = "placeId"
    }
}
