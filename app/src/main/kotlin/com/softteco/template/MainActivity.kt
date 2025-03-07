package com.softteco.template

import android.app.Activity
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import com.softteco.template.data.bluetooth.BluetoothHelper
import com.softteco.template.data.device.Device
import com.softteco.template.data.device.protocol.common.DeviceOperationHandler
import com.softteco.template.data.zigbee.ZigbeeHelper
import com.softteco.template.navigation.Graph
import com.softteco.template.ui.AppContent
import com.softteco.template.ui.components.dialog.DialogController
import com.softteco.template.ui.components.snackbar.SnackbarController
import com.softteco.template.ui.theme.AppTheme
import com.softteco.template.utils.protocol.checkBluetoothSupport
import com.softteco.template.utils.protocol.getDeviceImage
import com.softteco.template.utils.protocol.getDeviceModel
import com.softteco.template.utils.protocol.checkEnableDeviceModules
import com.softteco.template.utils.protocol.hasPermissions
import com.softteco.template.utils.protocol.registerCustomReceiver
import com.softteco.template.utils.protocol.startConnectionService
import com.softteco.template.utils.protocol.stopConnectionService
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity(), DeviceOperationHandler {

    private val viewModel: MainViewModel by viewModels()

    @Inject
    lateinit var snackbarController: SnackbarController

    @Inject
    lateinit var dialogController: DialogController

    @Inject
    lateinit var bluetoothHelper: BluetoothHelper

    @Inject
    lateinit var zigbeeHelper: ZigbeeHelper

    private val resultIntentLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        bluetoothHelper.init(this)
        zigbeeHelper.init(this)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            val isUserLoggedIn by viewModel.isUserLoggedIn.collectAsState()
            val theme by viewModel.theme.collectAsState()

            // Keep the splash screen on-screen while we check if the user is logged in
            val content: View = findViewById(android.R.id.content)
            content.viewTreeObserver.addOnPreDrawListener(
                object : ViewTreeObserver.OnPreDrawListener {
                    override fun onPreDraw(): Boolean {
                        return if (isUserLoggedIn != null) {
                            content.viewTreeObserver.removeOnPreDrawListener(this)
                            true
                        } else {
                            false
                        }
                    }
                }
            )

            key(isUserLoggedIn) {
                AppTheme(theme) {
                    when (isUserLoggedIn) {
                        true -> AppContent(
                            Graph.BottomBar.route,
                            snackbarController,
                            dialogController
                        )

                        false -> AppContent(Graph.Login.route, snackbarController, dialogController)
                        null -> { /*NOOP*/
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        bluetoothHelper.shutdown()
        zigbeeHelper.drop()
    }

    override fun getContext(): Context = applicationContext

    override fun getDeviceModel(deviceName: String): Device.Model =
        applicationContext.getDeviceModel(deviceName)

    override fun getDeviceImage(deviceName: String): String =
        applicationContext.getDeviceImage(deviceName)

    override fun startIntent(intent: Intent) {
        resultIntentLauncher.launch(intent)
    }

    override fun registerReceiver(receiver: BroadcastReceiver, filter: IntentFilter) {
        registerCustomReceiver(receiver, filter)
    }

    override fun startConnectionService(serviceClass: Class<out Service>) {
        applicationContext.startConnectionService(serviceClass)
    }

    override fun stopConnectionService(serviceClass: Class<out Service>) {
        applicationContext.stopConnectionService(serviceClass)
    }

    override fun checkBluetoothSupport() = applicationContext.checkBluetoothSupport()

    override fun checkEnableDeviceModules() = applicationContext.checkEnableDeviceModules()

    override fun hasPermissions() = (this as Activity).hasPermissions()
}
