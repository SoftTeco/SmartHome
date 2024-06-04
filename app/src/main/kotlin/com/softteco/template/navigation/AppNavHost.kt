package com.softteco.template.navigation

import android.app.Activity
import android.net.Uri
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import androidx.navigation.navigation
import com.softteco.template.data.device.Device.Type.RobotVacuum
import com.softteco.template.data.device.Device.Type.TemperatureAndHumidity
import com.softteco.template.navigation.AppNavHost.DEEP_LINK_URI
import com.softteco.template.navigation.AppNavHost.DEVICE_ID_KEY
import com.softteco.template.navigation.AppNavHost.RESET_PASSWORD_PATH
import com.softteco.template.navigation.AppNavHost.RESET_TOKEN_ARG
import com.softteco.template.ui.feature.deviceDashboard.deviceSettings.DeviceSettingsScreen
import com.softteco.template.ui.feature.deviceDashboard.devices.robotVacuum.RobotVacuumDashboardScreen
import com.softteco.template.ui.feature.deviceDashboard.devices.thermometer.ThermometerDashboardScreen
import com.softteco.template.ui.feature.forgotPassword.ForgotPasswordScreen
import com.softteco.template.ui.feature.home.HomeScreen
import com.softteco.template.ui.feature.home.device.connection.AddNewDeviceScreen
import com.softteco.template.ui.feature.home.device.connection.ScanQRScreen
import com.softteco.template.ui.feature.home.device.connection.manual.ManualSelectionScreen
import com.softteco.template.ui.feature.home.device.connection.manual.SearchDeviceScreen
import com.softteco.template.ui.feature.home.notifications.NotificationsScreen
import com.softteco.template.ui.feature.home.search.SearchScreen
import com.softteco.template.ui.feature.login.LoginScreen
import com.softteco.template.ui.feature.openSourceLicenses.OpenSourceLicensesScreen
import com.softteco.template.ui.feature.profile.ProfileScreen
import com.softteco.template.ui.feature.resetPassword.ResetPasswordScreen
import com.softteco.template.ui.feature.settings.SettingsScreen
import com.softteco.template.ui.feature.signUp.SignUpScreen

object AppNavHost {
    const val DEEP_LINK_URI = "https://template.softteco.com.deep_link"
    const val RESET_PASSWORD_PATH = "reset_password"
    const val RESET_TOKEN_ARG = "token"
    const val DEVICE_ID_KEY = "device_id"
}

@Composable
fun AppNavHost(
    navController: NavHostController,
    startDestination: String,
    paddingValues: PaddingValues,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
    ) {
        bottomBarGraph(navController, Modifier.padding(paddingValues = paddingValues))
        loginGraph(navController)
        settingsGraph(navController)
        homeGraph(navController)
    }

    RemoveDeepLink()
}

fun NavGraphBuilder.bottomBarGraph(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    navigation(
        startDestination = Screen.Home.route,
        route = Graph.BottomBar.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                modifier = modifier,
                onAddNewClick = { navController.navigate(Screen.AddNewDevice.route) },
                onSearchClick = { navController.navigate(Screen.Search.route) },
                onNotificationsClick = { navController.navigate(Screen.Notifications.route) },
                onDeviceClick = { device ->
                    val deviceId = device.id.toString()
                    when (device.type) {
                        TemperatureAndHumidity -> {
                            navController.navigate(Screen.ThermometerDashboard.createRoute(deviceId))
                        }
                        RobotVacuum -> {
                            navController.navigate(Screen.RobotVacuumDashboard.createRoute(deviceId))
                        }
                        else -> { /*TODO*/ }
                    }
                }
            )
        }
        composable(Screen.Profile.route) {
            ProfileScreen(
                modifier = modifier,
                onBackClicked = { navController.popBackStack() },
                onLogout = {
                    navController.navigate(Graph.Login.route) {
                        popUpTo(Graph.BottomBar.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Settings.route) {
            SettingsScreen(
                modifier = modifier,
                onBackClicked = { navController.navigateUp() },
                onLicensesClicked = {
                    navController.navigate(Screen.OpenSourceLicenses.route)
                }
            )
        }
    }
}

fun NavGraphBuilder.loginGraph(navController: NavController) {
    navigation(
        startDestination = Screen.Login.route,
        route = Graph.Login.route
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onBackClicked = { navController.navigateUp() },
                onSuccess = {
                    navController.navigate(Graph.BottomBar.route) {
                        popUpTo(Graph.BottomBar.route) { inclusive = true }
                    }
                },
                onSignUpClicked = { navController.navigate(Screen.SignUp.route) },
                onForgotPasswordClicked = { navController.navigate(Screen.ForgotPassword.route) }
            )
        }
        composable(Screen.SignUp.route) {
            SignUpScreen(
                onBackClicked = { navController.navigateUp() },
                onSuccess = { navController.navigate(Screen.Login.route) }
            )
        }
        composable(
            route = Screen.ResetPassword.route,
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = "$DEEP_LINK_URI/$RESET_PASSWORD_PATH/{$RESET_TOKEN_ARG}"
                }
            )
        ) {
            ResetPasswordScreen(
                onSuccess = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Graph.Login.route) { inclusive = true }
                    }
                },
            )
        }
        composable(Screen.ForgotPassword.route) {
            ForgotPasswordScreen(
                onBackClicked = { navController.navigateUp() },
                onSuccess = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Graph.Login.route) { inclusive = true }
                    }
                },
                navigateToSignUp = { navController.navigate(Screen.SignUp.route) },
            )
        }
    }
}

fun NavGraphBuilder.settingsGraph(navController: NavController) {
    navigation(
        startDestination = Screen.Settings.route,
        route = Graph.Settings.route,
    ) {
        composable(Screen.OpenSourceLicenses.route) {
            OpenSourceLicensesScreen(onBackClicked = { navController.navigateUp() })
        }
    }
}

fun NavGraphBuilder.homeGraph(navController: NavController, modifier: Modifier = Modifier) {
    navigation(
        startDestination = Screen.Home.route,
        route = Graph.Home.route
    ) {
        composable(Screen.AddNewDevice.route) {
            AddNewDeviceScreen(
                onBackClicked = { navController.navigateUp() },
                onScanClick = { navController.navigate(Screen.ScanQR.route) },
                onManualClick = { navController.navigate(Screen.ManualSelection.route) },
                modifier
            )
        }
        composable(Screen.ScanQR.route) {
            ScanQRScreen(onBackClicked = { navController.navigateUp() }, modifier)
        }
        composable(Screen.ManualSelection.route) {
            ManualSelectionScreen(
                onBackClicked = { navController.navigateUp() },
                onSearchClick = { navController.navigate(Screen.SearchDevice.route) },
                onDeviceClick = { /*TODO*/ },
                modifier
            )
        }
        composable(Screen.SearchDevice.route) {
            SearchDeviceScreen(
                onBackClicked = { navController.navigateUp() },
                onDeviceClick = { /*TODO*/ },
                modifier
            )
        }

        composable(Screen.Search.route) {
            SearchScreen(onBackClicked = { navController.navigateUp() }, modifier)
        }
        composable(Screen.Notifications.route) {
            NotificationsScreen(onBackClicked = { navController.navigateUp() }, modifier)
        }
        composable(
            route = Screen.ThermometerDashboard.route,
            arguments = listOf(navArgument(DEVICE_ID_KEY) { type = NavType.StringType })
        ) {
            ThermometerDashboardScreen(
                onSettingsClick = { deviceId ->
                    navController.navigate(Screen.DeviceSettings.createRoute(deviceId))
                },
                onBackClicked = { navController.navigateUp() },
                modifier = modifier,
            )
        }
        composable(
            route = Screen.RobotVacuumDashboard.route,
            arguments = listOf(navArgument(DEVICE_ID_KEY) { type = NavType.StringType })
        ) {
            RobotVacuumDashboardScreen(
                onSettingsClick = { deviceId ->
                    navController.navigate(Screen.DeviceSettings.createRoute(deviceId))
                },
                onBackClicked = { navController.navigateUp() },
                modifier = modifier,
            )
        }
        composable(
            route = Screen.DeviceSettings.route,
            arguments = listOf(navArgument(DEVICE_ID_KEY) { type = NavType.StringType })
        ) {
            DeviceSettingsScreen(
                onBackClicked = { navController.navigateUp() },
                modifier = modifier,
            )
        }
    }
}

/*
 * It looks like the navigation library tries to get a deep link from Intent every time it
 * recomposes NavHost. This results in an unwanted screen opening on the link.
 * Removing the link after creating the navigation graph fixes this problem.
 * Issue: [#161](https://github.com/SoftTeco/AndroidAppTemplate/issues/161);
 */
@Composable
private fun RemoveDeepLink() {
    val activity = LocalContext.current as? Activity
    val intent = activity?.intent
    val isDeepLink = intent?.data?.host == Uri.parse(DEEP_LINK_URI).host
    if (isDeepLink) {
        activity?.intent = null
    }
}
