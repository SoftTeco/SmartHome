package com.softteco.template.ui.feature.devicesearch

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.WifiTethering
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.softteco.template.R

@Composable
fun DeviceSearchScreen(
    modifier: Modifier = Modifier,
    onBackClicked: () -> Unit,
    firstProtocolSelected: (deviceName: String) -> Unit,
    secondProtocolSelected: (deviceName: String) -> Unit,
    viewModel: DeviceSearchViewModel = hiltViewModel()
) {

    val state by viewModel.state.collectAsState()

    ScreenContent(
        modifier = modifier,
        onBackClicked = onBackClicked,
        firstProtocolSelected = firstProtocolSelected,
        secondProtocolSelected = secondProtocolSelected,
        deviceName = state.deviceName
    )
}

@Composable
private fun ScreenContent(
    modifier: Modifier = Modifier,
    onBackClicked: () -> Unit,
    firstProtocolSelected: (deviceName: String) -> Unit,
    secondProtocolSelected: (deviceName: String) -> Unit,
    deviceName: String
) {
    Column(modifier = modifier.fillMaxSize()) {
        TopAppBarWithBurgerMenu(
            onBackClicked = onBackClicked,
            firstProtocolSelected = firstProtocolSelected,
            secondProtocolSelected = secondProtocolSelected,
            deviceName
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBarWithBurgerMenu(
    onBackClicked: () -> Unit,
    firstProtocolSelected: (deviceName: String) -> Unit,
    secondProtocolSelected: (deviceName: String) -> Unit,
    deviceName: String
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
                Icon(Icons.Outlined.ArrowBack, contentDescription = stringResource(R.string.back_description))
            }
        },
        actions = {
            IconButton(onClick = { expanded = true }) {
                Icon(Icons.Outlined.WifiTethering, contentDescription = stringResource(R.string.protocol_selection))
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
        modifier = Modifier.statusBarsPadding()
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
        ""
    )
}

// endregion
