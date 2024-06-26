package com.softteco.template.ui.feature.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import com.softteco.template.R
import com.softteco.template.ui.components.CustomTopAppBar
import com.softteco.template.ui.components.DeviceImage
import com.softteco.template.ui.components.OnLifecycleEvent
import com.softteco.template.ui.components.PrimaryButton
import com.softteco.template.ui.theme.Dimens
import com.softteco.template.utils.bluetooth.BluetoothDeviceConnectionStatus
import com.softteco.template.utils.bluetooth.getBluetoothDeviceModel
import com.softteco.template.utils.bluetooth.getBluetoothDeviceName

@Composable
fun BluetoothScreen(
    onOpenDashboard: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: BluetoothViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val deviceName = stringResource(R.string.temperature_and_humidity_LYWSD03MMC)

    LaunchedEffect(Unit) {
        viewModel.filteredName = deviceName
        viewModel.onScanCallback {
            viewModel.emitDeviceConnectionStatusList()
            viewModel.addScanResult(it)
        }
        viewModel.onConnectCallback {
            viewModel.emitDeviceConnectionStatusList()
        }

        viewModel.onDisconnectCallback {
            viewModel.emitDeviceConnectionStatusList()
        }

        viewModel.onBluetoothModuleChangeStateCallback {
            if (!it) {
                viewModel.clearScanResult()
            }
        }
    }

    val onFilter: (checked: Boolean) -> Unit = {
        viewModel.setFiltered(it)
    }
    ScreenContent(
        state = state,
        onItemClicked = { bluetoothDevice ->
            viewModel.provideConnectionToDevice(bluetoothDevice)
        },
        onShowChart = onOpenDashboard,
        onSetCurrentlyViewedBluetoothDeviceAddress = { bluetoothDeviceAddress ->
            viewModel.setCurrentlyViewedBluetoothDeviceAddress(bluetoothDeviceAddress)
        },
        onFilter,
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
    onShowChart: () -> Unit,
    onSetCurrentlyViewedBluetoothDeviceAddress: (String) -> Unit,
    onFilter: (checked: Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        CustomTopAppBar(
            stringResource(id = R.string.temperature_and_humidity),
            modifier = Modifier.fillMaxWidth()
        )
        BluetoothDevicesSwitch(onFilter)
        BluetoothDevicesList(
            devices = state.devices,
            devicesConnectionStatusList = state.devicesConnectionStatusList,
            onItemClicked = onItemClicked,
            onShowChart = onShowChart,
            onSetCurrentlyViewedBluetoothDeviceAddress = onSetCurrentlyViewedBluetoothDeviceAddress
        )
    }
}

@Composable
fun BluetoothDevicesSwitch(
    onFilter: (checked: Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var checked by remember { mutableStateOf(true) }
    Row(
        modifier = modifier.padding(start = Dimens.PaddingDefault)
    ) {
        Text(
            modifier = Modifier
                .align(alignment = Alignment.CenterVertically)
                .padding(end = Dimens.PaddingSmall),
            text = stringResource(id = R.string.show_only_thermometers),
            overflow = TextOverflow.Ellipsis,
            maxLines = 1
        )
        Switch(
            checked = checked,
            onCheckedChange = {
                checked = it
                onFilter.invoke(checked)
            }
        )
    }
}

@Composable
fun BluetoothDevicesList(
    devices: List<BluetoothDevice>,
    devicesConnectionStatusList: List<BluetoothDeviceConnectionStatus>,
    onItemClicked: (BluetoothDevice) -> Unit,
    onShowChart: () -> Unit,
    onSetCurrentlyViewedBluetoothDeviceAddress: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier.fillMaxSize()) {
        devices.forEach { device ->
            item {
                devicesConnectionStatusList.forEach {
                    if (it.bluetoothDevice.macAddress == device.address) {
                        BluetoothDeviceCard(
                            bluetoothDevice = device,
                            connectionStatus = it.isConnected,
                            onItemClicked = onItemClicked,
                            onShowChart = onShowChart,
                            onSetCurrentlyViewedBluetoothDeviceAddress = onSetCurrentlyViewedBluetoothDeviceAddress
                        )
                    }
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
    onShowChart: () -> Unit,
    onSetCurrentlyViewedBluetoothDeviceAddress: (String) -> Unit,
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
                .clickable {
                    if (connectionStatus == true) {
                        onShowChart.invoke()
                        onSetCurrentlyViewedBluetoothDeviceAddress.invoke(bluetoothDevice.address)
                    }
                }
                .padding(Dimens.PaddingSmall),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val deviceModel = context.getBluetoothDeviceModel(bluetoothDevice.name)
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
