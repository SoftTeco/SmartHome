package com.softteco.template.ui.feature.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.softteco.template.R
import com.softteco.template.data.device.Device
import com.softteco.template.navigation.Screen
import com.softteco.template.ui.components.DeviceImage
import com.softteco.template.ui.components.PreviewStub
import com.softteco.template.ui.components.ProtocolImage
import com.softteco.template.ui.theme.AppTheme
import com.softteco.template.ui.theme.Dimens.PaddingDefault
import com.softteco.template.ui.theme.Dimens.PaddingNormal
import com.softteco.template.ui.theme.Dimens.PaddingSmall
import com.softteco.template.utils.Analytics
import com.softteco.template.utils.protocol.DeviceConnectionStatus
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@Composable
fun HomeScreen(
    onAddNewClick: () -> Unit,
    onSearchClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onDeviceClick: (Device) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    val showDeleteDialog = remember { mutableStateOf<Device?>(null) }

    LaunchedEffect(Unit) {
        Analytics.homeOpened()
        viewModel.navDestination.onEach { screen ->
            when (screen) {
                Screen.AddNewDevice -> onAddNewClick()
                Screen.Search -> onSearchClick()
                Screen.Notifications -> onNotificationsClick()

                else -> { /*NOOP*/
                }
            }
        }.launchIn(this)
        viewModel.getDeviceConnectionStatusList()
        viewModel.getDevices()
    }

    ScreenContent(
        state,
        onAddNewClick,
        onSearchClick,
        onNotificationsClick,
        { device ->
            viewModel.performDeviceClick(
                onDeviceClick,
                state.devicesConnectionStatusList,
                device
            )
        },
        { device -> showDeleteDialog.value = device },
        { device -> viewModel.getDeviceConnectionStatus(device, state.devicesConnectionStatusList) },
        modifier
    )

    showDeleteDialog.value?.let { device ->
        DeleteDeviceDialog(
            device = device,
            onConfirm = {
                viewModel.deleteDevice(device)
                showDeleteDialog.value = null
            },
            onDismiss = { showDeleteDialog.value = null }
        )
    }
}

@Composable
private fun ScreenContent(
    state: HomeViewModel.State,
    onAddNewClick: () -> Unit,
    onSearchClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onDeviceClick: (Device) -> Unit,
    onLongClick: (Device) -> Unit,
    getDeviceConnectionStatus: (Device) -> DeviceConnectionStatus?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier
            .background(MaterialTheme.colorScheme.background)
            .fillMaxSize(),
    ) {
        TopAppBar(
            title = stringResource(R.string.default_home_name),
            onAddNewClick = onAddNewClick,
            onSearchClick = onSearchClick,
            onNotificationsClick = onNotificationsClick,
        )

        if (state.devices.isNotEmpty()) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(PaddingDefault),
                horizontalArrangement = Arrangement.spacedBy(PaddingDefault),
                contentPadding = PaddingValues(
                    horizontal = PaddingDefault,
                    vertical = PaddingNormal
                ),
            ) {
                items(state.devices) {
                    Device(
                        it,
                        getDeviceConnectionStatus(it),
                        onClick = { onDeviceClick(it) },
                        onLongClick
                    )
                }
            }
        } else {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                TextButton(
                    onClick = onAddNewClick,
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = PaddingDefault, vertical = PaddingNormal)
                ) {
                    Icon(Icons.Outlined.Add, null)
                    Text(
                        stringResource(R.string.add_new_device),
                        Modifier.padding(start = PaddingSmall)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopAppBar(
    title: String,
    onAddNewClick: () -> Unit,
    onSearchClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface {
        TopAppBar(
            title = { Text(text = title) },
            modifier = modifier,
            navigationIcon = {},
            actions = {
                IconButton(onClick = onAddNewClick) {
                    Icon(
                        imageVector = Icons.Outlined.Add,
                        contentDescription = stringResource(R.string.add_new_device)
                    )
                }
                IconButton(onClick = onSearchClick) {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = stringResource(R.string.search)
                    )
                }
                IconButton(onClick = onNotificationsClick) {
                    Icon(
                        imageVector = Icons.Outlined.Notifications,
                        contentDescription = stringResource(R.string.notifications)
                    )
                }
            },
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun Device(
    device: Device,
    deviceConnectionStatus: DeviceConnectionStatus?,
    onClick: () -> Unit,
    onLongClick: (Device) -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier.combinedClickable(
            onClick = onClick,
            onLongClick = { onLongClick(device) }
        )
    ) {
        Column(Modifier.padding(PaddingSmall)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                DeviceImage(
                    imageUri = device.img,
                    Modifier.size(48.dp),
                )
                ProtocolImage(
                    device,
                    deviceConnectionStatus,
                    Modifier.size(24.dp),
                )
                if (device is Device.QuickAccess) {
                    IconButton(onClick = device.onClickAction) {
                        Icon(
                            painterResource(device.actionIcon),
                            contentDescription = stringResource(device.actionDescription)
                        )
                    }
                }
            }
            Spacer(Modifier.height(PaddingSmall))
            Column {
                Text(
                    device.name,
                    style = MaterialTheme.typography.labelMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    device.location,
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                // Show status indicator for all connection states
                deviceConnectionStatus?.let { status ->
                    when (status.connectionState) {
                        com.softteco.template.utils.protocol.ConnectionState.SEARCHING -> {
                            Row(
                                modifier = Modifier.padding(top = PaddingSmall),
                                horizontalArrangement = Arrangement.spacedBy(PaddingSmall),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(12.dp),
                                    strokeWidth = 1.5.dp
                                )
                                Text(
                                    text = stringResource(R.string.searching),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        com.softteco.template.utils.protocol.ConnectionState.CONNECTING -> {
                            Row(
                                modifier = Modifier.padding(top = PaddingSmall),
                                horizontalArrangement = Arrangement.spacedBy(PaddingSmall),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(12.dp),
                                    strokeWidth = 1.5.dp
                                )
                                Text(
                                    text = stringResource(R.string.connecting),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        com.softteco.template.utils.protocol.ConnectionState.CONNECTED -> {
                            Text(
                                text = stringResource(R.string.connected),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF4CAF50), // Green
                                modifier = Modifier.padding(top = PaddingSmall)
                            )
                        }
                        com.softteco.template.utils.protocol.ConnectionState.DISCONNECTED -> {
                            Text(
                                text = stringResource(R.string.disconnected),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.error, // Red
                                modifier = Modifier.padding(top = PaddingSmall)
                            )
                        }
                        else -> {}
                    }
                }
            }
        }
    }
}

@Composable
private fun DeleteDeviceDialog(
    device: Device,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.delete_device_title)) },
        text = { Text(stringResource(R.string.delete_device_message, device.name)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(R.string.delete))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

// region Previews

@Preview(showSystemUi = true)
@Composable
private fun Preview() {
    AppTheme {
        ScreenContent(
            state = HomeViewModel.State(),
            onAddNewClick = {},
            onSearchClick = {},
            onNotificationsClick = {},
            onDeviceClick = {},
            onLongClick = {},
            getDeviceConnectionStatus = { null }
        )
    }
}

@Preview(showSystemUi = true)
@Composable
private fun PreviewDevices() {
    AppTheme {
        ScreenContent(
            state = HomeViewModel.State(PreviewStub.connectedDevices),
            onAddNewClick = {},
            onSearchClick = {},
            onNotificationsClick = {},
            onDeviceClick = {},
            onLongClick = {},
            getDeviceConnectionStatus = { null }
        )
    }
}

// endregion
