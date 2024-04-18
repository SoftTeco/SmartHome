package com.softteco.template.navigation

sealed class Graph(val route: String) {
    data object Home : Graph("home_graph")
    data object Profile : Graph("profile_graph")
    data object Settings : Graph("settings_graph")
    data object BottomBar : Graph("bottom_bar_graph")
    data object Login : Graph("login_graph")
}
