package com.smartpoultry.app.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Dashboard : Screen("dashboard")
    object Prediction : Screen("prediction")
    object History : Screen("history")
    object Profile : Screen("profile")
    object Settings : Screen("settings")
}
