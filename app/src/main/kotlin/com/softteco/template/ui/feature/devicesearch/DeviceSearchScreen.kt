package com.softteco.template.ui.feature.devicesearch

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.WifiTethering
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.softteco.template.R

@Composable
fun DeviceSearchScreen(
    onBackClicked: () -> Unit,
    firstProtocolSelected: (deviceName: String) -> Unit,
    secondProtocolSelected: (deviceName: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DeviceSearchViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    ScreenContent(
        onBackClicked = onBackClicked,
        firstProtocolSelected = firstProtocolSelected,
        secondProtocolSelected = secondProtocolSelected,
        deviceName = state.deviceName,
        modifier = modifier.fillMaxSize()
    )
}

@Composable
private fun ScreenContent(
    onBackClicked: () -> Unit,
    firstProtocolSelected: (deviceName: String) -> Unit,
    secondProtocolSelected: (deviceName: String) -> Unit,
    deviceName: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        TopAppBarWithBurgerMenu(
            onBackClicked = onBackClicked,
            firstProtocolSelected = firstProtocolSelected,
            secondProtocolSelected = secondProtocolSelected,
            deviceName = deviceName
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBarWithBurgerMenu(
    onBackClicked: () -> Unit,
    firstProtocolSelected: (deviceName: String) -> Unit,
    secondProtocolSelected: (deviceName: String) -> Unit,
    deviceName: String,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val protocols = listOf(
        stringResource(id = R.string.bluetooth) to firstProtocolSelected,
        stringResource(id = R.string.zigbee) to secondProtocolSelected
    )

    TopAppBar(
        title = { Text(stringResource(R.string.settings)) },
        navigationIcon = {
            IconButton(onClick = onBackClicked) {
                Icon(
                    Icons.Outlined.ArrowBack,
                    contentDescription = stringResource(R.string.back_description)
                )
            }
        },
        actions = {
            IconButton(onClick = { expanded = true }) {
                Icon(
                    Icons.Outlined.WifiTethering,
                    contentDescription = stringResource(R.string.protocol_selection)
                )
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                protocols.forEach { protocol ->
                    DropdownMenuItem(
                        text = { Text(protocol.first) },
                        onClick = {
                            expanded = false
                            protocol.second.invoke(deviceName)
                        }
                    )
                }
            }
        },
        modifier = modifier.statusBarsPadding()
    )
}

// region Previews

@Preview(showBackground = true)
@Composable
fun PreviewTopAppBarWithBurgerMenu() {
    TopAppBarWithBurgerMenu(
        onBackClicked = {},
        firstProtocolSelected = {},
        secondProtocolSelected = {},
        deviceName = "",
        modifier = Modifier
    )
}

// endregion
