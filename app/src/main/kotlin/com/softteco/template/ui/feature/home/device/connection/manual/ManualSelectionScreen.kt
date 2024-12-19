package com.softteco.template.ui.feature.home.device.connection.manual

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.softteco.template.R
import com.softteco.template.data.device.Device
import com.softteco.template.data.device.Device.Family.Sensor.getName
import com.softteco.template.data.device.Device.Model.Unknown.getName
import com.softteco.template.data.device.SupportedDevice
import com.softteco.template.data.device.SupportedDevices
import com.softteco.template.ui.components.DeviceImage
import com.softteco.template.ui.components.PreviewStub.supportedDevices
import com.softteco.template.ui.theme.AppTheme
import com.softteco.template.ui.theme.Dimens.PaddingDefault
import com.softteco.template.ui.theme.Dimens.PaddingNormal
import com.softteco.template.ui.theme.Dimens.PaddingSmall

private const val GRID_CELL_COUNT = 2

@Composable
fun ManualSelectionScreen(
    onBackClicked: () -> Unit,
    onSearchClick: () -> Unit,
    onDeviceClick: (SupportedDevice, String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ManualSelectionViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    ScreenContent(state, onBackClicked, onSearchClick, onDeviceClick, modifier)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScreenContent(
    state: ManualSelectionViewModel.State,
    onBackClicked: () -> Unit,
    onSearchClick: () -> Unit,
    onDeviceClick: (SupportedDevice, String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier
            .background(MaterialTheme.colorScheme.background)
            .fillMaxSize()
    ) {
        TopAppBar(
            title = { Text(stringResource(R.string.manual_selection_title)) },
            navigationIcon = {
                IconButton(onClick = onBackClicked) {
                    Icon(Icons.Outlined.ArrowBack, stringResource(R.string.back_description))
                }
            },
            actions = {
                IconButton(onClick = onSearchClick) {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = stringResource(R.string.search)
                    )
                }
            }
        )

        val context = LocalContext.current
        val devicesByFamily by remember {
            derivedStateOf {
                state.devices
                    .groupBy { it.supportedDevice.family }
                    .toList()
                    .sortedBy { it.first.getName(context) }
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(GRID_CELL_COUNT),
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(PaddingDefault),
            contentPadding = PaddingValues(
                start = PaddingDefault,
                top = PaddingNormal,
                end = PaddingDefault,
            ),
        ) {
            devicesByFamily.forEach { familyDevicesPair: Pair<Device.Family, List<SupportedDevices>> ->
                item(span = { GridItemSpan(GRID_CELL_COUNT) }) {
                    Text(
                        familyDevicesPair.first.toString(),
                        style = MaterialTheme.typography.titleSmall,
                    )
                }
                items(familyDevicesPair.second.sortedBy { it.supportedDevice.type.toString() }) {
                    Device(
                        device = it.supportedDevice,
                        onClick = { onDeviceClick(it.supportedDevice, it.supportedDevice.model.getName(context)) },
                        Modifier.padding(top = 12.dp)
                    )
                }
                item(span = { GridItemSpan(GRID_CELL_COUNT) }) {
                    Spacer(Modifier.height(PaddingNormal))
                }
            }
        }
    }
}

@Composable
private fun Device(device: SupportedDevice, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current

    ElevatedCard(onClick = onClick, modifier = modifier) {
        Row(Modifier.padding(PaddingSmall), verticalAlignment = Alignment.CenterVertically) {
            DeviceImage(
                imageUri = device.img,
                Modifier.size(32.dp),
                name = device.type.getName(context),
            )
            Spacer(Modifier.width(PaddingSmall))
            Text(
                stringResource(device.type.nameRes),
                style = MaterialTheme.typography.labelSmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

// region Previews

@Preview(showSystemUi = true)
@Composable
private fun Preview() {
    AppTheme {
        ScreenContent(
            state = ManualSelectionViewModel.State(supportedDevices),
            onBackClicked = {},
            onSearchClick = {},
            onDeviceClick = { _, _ -> },
        )
    }
}

// endregion
