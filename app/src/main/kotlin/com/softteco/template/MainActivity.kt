package com.softteco.template

import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import com.softteco.template.data.bluetooth.BluetoothHelper
import com.softteco.template.data.zigbee.ZigbeeHelper
import com.softteco.template.navigation.Graph
import com.softteco.template.ui.AppContent
import com.softteco.template.ui.components.dialog.DialogController
import com.softteco.template.ui.components.snackbar.SnackbarController
import com.softteco.template.ui.theme.AppTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    @Inject
    lateinit var snackbarController: SnackbarController

    @Inject
    lateinit var dialogController: DialogController

    @Inject
    lateinit var bluetoothHelper: BluetoothHelper

    @Inject
    lateinit var zigbeeHelper: ZigbeeHelper

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
                        true -> AppContent(Graph.BottomBar.route, snackbarController, dialogController)
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
        bluetoothHelper.drop()
        zigbeeHelper.drop()
    }
}
