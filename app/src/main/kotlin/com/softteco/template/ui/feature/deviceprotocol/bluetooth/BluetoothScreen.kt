package com.softteco.template.ui.feature.deviceprotocol.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import com.softteco.template.R
import com.softteco.template.ui.components.CustomTopAppBar
import com.softteco.template.ui.components.DeviceImage
import com.softteco.template.ui.components.OnLifecycleEvent
import com.softteco.template.ui.components.PrimaryButton
import com.softteco.template.ui.theme.AppTheme
import com.softteco.template.ui.theme.Dimens
import com.softteco.template.utils.protocol.DeviceConnectionStatus
import com.softteco.template.utils.protocol.getBluetoothDeviceName
import com.softteco.template.utils.protocol.getDeviceModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun BluetoothScreen(
    onBackClicked: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: BluetoothViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.initCallbacks()
        viewModel.getDeviceConnectionStatusList()
    }

    ScreenContent(
        state = state,
        onItemClicked = { bluetoothDevice ->
            CoroutineScope(Dispatchers.IO).launch {
                viewModel.provideConnectionToDevice(bluetoothDevice)
            }
        },
        onBackClicked,
        modifier = modifier
    )
    OnLifecycleEvent { owner, event ->
        when (event) {
            Lifecycle.Event.ON_RESUME -> {
                viewModel.registerReceiver()
                viewModel.startScanIfHasPermissions()
            }

            Lifecycle.Event.ON_PAUSE -> {
                viewModel.unregisterReceiver()
            }

            else -> {}
        }
    }
}

@Composable
private fun ScreenContent(
    state: BluetoothViewModel.State,
    onItemClicked: (BluetoothDevice) -> Unit,
    onBackClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        CustomTopAppBar(
            stringResource(id = R.string.bluetooth),
            showBackIcon = true,
            onBackClicked = onBackClicked,
            modifier = Modifier.fillMaxWidth()
        )
        BluetoothDevicesList(
            devices = state.devices,
            devicesConnectionStatusList = state.devicesConnectionStatusList,
            onItemClicked = onItemClicked
        )
    }
}

@Composable
fun BluetoothDevicesList(
    devices: List<BluetoothDevice>,
    devicesConnectionStatusList: List<DeviceConnectionStatus>,
    onItemClicked: (BluetoothDevice) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier.fillMaxSize()) {
        items(devices) { device ->
            devicesConnectionStatusList.forEach {
                if (it.device.macAddress == device.address) {
                    BluetoothDeviceCard(
                        bluetoothDevice = device,
                        connectionStatus = it.isConnected,
                        onItemClicked = onItemClicked
                    )
                }
            }
        }
    }
}

@SuppressLint("MissingPermission")
@Composable
fun BluetoothDeviceCard(
    bluetoothDevice: BluetoothDevice,
    connectionStatus: Boolean?,
    onItemClicked: (BluetoothDevice) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Card(
        modifier = modifier
            .padding(vertical = Dimens.PaddingSmall, horizontal = Dimens.PaddingDefault)
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(Dimens.PaddingSmall),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val deviceModel = context.getDeviceModel(bluetoothDevice.name)
            DeviceImage(
                imageUri = deviceModel.img,
                Modifier.size(32.dp),
                name = bluetoothDevice.name,
            )
            Column(Modifier.weight(1F)) {
                Text(
                    text = getBluetoothDeviceName(bluetoothDevice),
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1
                )
                Text(
                    text = bluetoothDevice.address,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1
                )
            }
            PrimaryButton(
                buttonText = stringResource(
                    id = if (connectionStatus == true) {
                        R.string.disconnect
                    } else {
                        R.string.connect
                    }
                ),
                loading = false,
                modifier = Modifier.weight(1F),
                enabled = true,
                onClick = { onItemClicked(bluetoothDevice) },
            )
        }
    }
}

@Preview
@Composable
private fun Preview() {
    AppTheme {
        ScreenContent(
            state = BluetoothViewModel.State(),
            onItemClicked = {},
            onBackClicked = {}
        )
    }
}
