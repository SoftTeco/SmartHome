package com.softteco.template.ui.feature.deviceprotocol.zigbee

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.softteco.template.R
import com.softteco.template.ui.components.CustomTopAppBar
import com.softteco.template.ui.components.DeviceImage
import com.softteco.template.ui.components.PrimaryButton
import com.softteco.template.ui.theme.AppTheme
import com.softteco.template.ui.theme.Dimens
import com.softteco.template.utils.ZigbeeDevice
import com.softteco.template.utils.protocol.DeviceConnectionStatus
import com.softteco.template.utils.protocol.getDeviceModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun ZigBeeScreen(
    onBackClicked: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ZigBeeViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    ScreenContent(
        state = state,
        onItemClicked = { deviceName -> viewModel.provideConnectionToDevice(deviceName) },
        onBackClicked,
        modifier = modifier
    )
}

@Composable
private fun ScreenContent(
    state: ZigBeeViewModel.State,
    onItemClicked: (String) -> Unit,
    onBackClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        CustomTopAppBar(
            stringResource(id = R.string.zigbee),
            showBackIcon = true,
            onBackClicked = onBackClicked,
            modifier = Modifier.fillMaxWidth()
        )
        Box(modifier = Modifier.fillMaxSize()) {
            ZigBeeDevicesList(
                devices = state.devices,
                devicesConnectionStatusList = state.devicesConnectionStatusList,
                onItemClicked = onItemClicked
            )
            
            if (state.devices.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background.copy(alpha = 0.9f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(64.dp),
                        strokeWidth = 4.dp
                    )
                }
            }
        }
    }
}

@Composable
fun ZigBeeDevicesList(
    devices: List<ZigbeeDevice>,
    devicesConnectionStatusList: List<DeviceConnectionStatus>,
    onItemClicked: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier.fillMaxSize()) {
        items(devices) { device ->
            devicesConnectionStatusList.forEach {
                if (it.device.macAddress == device.friendlyName) {
                    ZigBeeDeviceCard(
                        zigBeeDevice = device,
                        deviceConnectionStatus = it,
                        onItemClicked = onItemClicked
                    )
                }
            }
        }
    }
}

@SuppressLint("MissingPermission")
@Composable
fun ZigBeeDeviceCard(
    zigBeeDevice: ZigbeeDevice,
    deviceConnectionStatus: DeviceConnectionStatus,
    onItemClicked: (String) -> Unit,
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
            val deviceModel = context.getDeviceModel(zigBeeDevice.modelId ?: "")
            DeviceImage(
                imageUri = deviceModel.img,
                Modifier.size(32.dp),
                name = zigBeeDevice.modelId ?: "",
            )
            Column(Modifier.weight(1F)) {
                Text(
                    text = zigBeeDevice.modelId ?: "",
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1
                )
                Text(
                    text = zigBeeDevice.ieeeAddress,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1
                )
                
                // Show status indicator for SEARCHING or CONNECTING states
                when (deviceConnectionStatus.connectionState) {
                    com.softteco.template.utils.protocol.ConnectionState.SEARCHING -> {
                        Row(
                            modifier = Modifier.padding(top = Dimens.PaddingSmall),
                            horizontalArrangement = Arrangement.spacedBy(Dimens.PaddingSmall),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                            Text(
                                text = stringResource(R.string.searching),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    com.softteco.template.utils.protocol.ConnectionState.CONNECTING -> {
                        Row(
                            modifier = Modifier.padding(top = Dimens.PaddingSmall),
                            horizontalArrangement = Arrangement.spacedBy(Dimens.PaddingSmall),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                            Text(
                                text = stringResource(R.string.connecting),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    else -> {}
                }
            }
            PrimaryButton(
                buttonText = stringResource(
                    id = if (deviceConnectionStatus.connectionState == com.softteco.template.utils.protocol.ConnectionState.CONNECTED) {
                        R.string.disconnect
                    } else {
                        R.string.connect
                    }
                ),
                loading = deviceConnectionStatus.connectionState == com.softteco.template.utils.protocol.ConnectionState.CONNECTING,
                modifier = Modifier.weight(1F),
                onClick = { onItemClicked(zigBeeDevice.friendlyName) },
            )
        }
    }
}

@Preview
@Composable
private fun Preview() {
    AppTheme {
        ScreenContent(
            state = ZigBeeViewModel.State(),
            onItemClicked = {},
            onBackClicked = {}
        )
    }
}
