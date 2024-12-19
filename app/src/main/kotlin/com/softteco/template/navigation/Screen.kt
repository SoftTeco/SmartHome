package com.softteco.template.navigation

import com.softteco.template.navigation.AppNavHost.DEVICE_ID_KEY
import com.softteco.template.navigation.AppNavHost.DEVICE_MAC_ADDRESS
import com.softteco.template.navigation.AppNavHost.DEVICE_NAME_KEY
import com.softteco.template.navigation.AppNavHost.DEVICE_PROTOCOL

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

    data object ThermometerDashboard :
        Screen("thermometer_dashboard/{$DEVICE_ID_KEY}/{$DEVICE_PROTOCOL}/{$DEVICE_MAC_ADDRESS}") {
        fun createRoute(deviceId: String, deviceProtocol: String, deviceMacAddress: String) =
            "thermometer_dashboard/$deviceId/$deviceProtocol/$deviceMacAddress"
    }

    data object RobotVacuumDashboard : Screen("robot_vacuum_dashboard/{$DEVICE_ID_KEY}") {
        fun createRoute(deviceId: String) = "robot_vacuum_dashboard/$deviceId"
    }

    data object DeviceSettings : Screen("device_settings/{$DEVICE_ID_KEY}") {
        fun createRoute(deviceId: String) = "device_settings/$deviceId"
    }

    data object Bluetooth : Screen("bluetooth/{$DEVICE_NAME_KEY}") {
        fun createRoute(deviceName: String) = "bluetooth/$deviceName"
    }

    data object ZigBee : Screen("zigbee/{$DEVICE_NAME_KEY}") {
        fun createRoute(deviceName: String) = "zigbee/$deviceName"
    }

    data object DeviceSearch : Screen("device_search/{$DEVICE_NAME_KEY}") {
        fun createRoute(deviceName: String) = "device_search/$deviceName"
    }
}
