package com.softteco.template.navigation

sealed class Screen(val route: String) {
    data object Home : Screen("home")

    data object AddNewDevice : Screen("add_new_device")
    data object ScanQR : Screen("scan_qr")
    data object ManualSelection : Screen("manual_selection")
    data object SearchDevice : Screen("search_device")

    data object Search : Screen("search")
    data object Notifications : Screen("notifications")

    data object Profile : Screen("profile")
    data object Settings : Screen("settings")
    data object Login : Screen("login")
    data object SignUp : Screen("sign_up")
    data object ResetPassword : Screen("reset_password")
    data object ForgotPassword : Screen("forgot_password")
    data object OpenSourceLicenses : Screen("open_source_licenses")
}
