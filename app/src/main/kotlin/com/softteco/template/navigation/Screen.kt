package com.softteco.template.navigation

import com.softteco.template.navigation.AppNavHost.DEVICE_ID_KEY

sealed class Screen(val route: String) {
    data object Home : Screen("home")

    data object AddNewDevice : Screen("add_new_device")
    data object ScanQR : Screen("scan_qr")
    data object ManualSelection : Screen("manual_selection")
    data object SearchDevice : Screen("search_device")

    data object Bluetooth : Screen("bluetooth")

    data object Search : Screen("search")
    data object Notifications : Screen("notifications")

    data object Profile : Screen("profile")
    data object Settings : Screen("settings")
    data object Login : Screen("login")
    data object SignUp : Screen("sign_up")
    data object ResetPassword : Screen("reset_password")
    data object ForgotPassword : Screen("forgot_password")
    data object OpenSourceLicenses : Screen("open_source_licenses")

    data object ThermometerDashboard : Screen("thermometer_dashboard/{$DEVICE_ID_KEY}") {
        fun createRoute(deviceId: String) = "thermometer_dashboard/$deviceId"
    }

    data object RobotVacuumDashboard : Screen("robot_vacuum_dashboard/{$DEVICE_ID_KEY}") {
        fun createRoute(deviceId: String) = "robot_vacuum_dashboard/$deviceId"
    }

    data object DeviceSettings : Screen("device_settings/{$DEVICE_ID_KEY}") {
        fun createRoute(deviceId: String) = "device_settings/$deviceId"
    }
}
