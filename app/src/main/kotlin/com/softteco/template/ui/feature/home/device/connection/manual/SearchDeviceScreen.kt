package com.softteco.template.ui.feature.home.device.connection.manual

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.softteco.template.R
import com.softteco.template.data.device.Device.Family.Cleaning.getName
import com.softteco.template.data.device.SupportedDevice
import com.softteco.template.data.device.SupportedDevices
import com.softteco.template.ui.components.DeviceImage
import com.softteco.template.ui.components.PreviewStub.supportedDevices
import com.softteco.template.ui.theme.AppTheme
import com.softteco.template.ui.theme.Dimens.PaddingDefault
import com.softteco.template.ui.theme.Dimens.PaddingNormal
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce

private const val INPUT_DELAY: Long = 500

@Composable
fun SearchDeviceScreen(
    onBackClicked: () -> Unit,
    onDeviceClick: (SupportedDevice) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ManualSelectionViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    ScreenContent(state, onBackClicked, onDeviceClick, modifier)
}

@OptIn(FlowPreview::class)
@Composable
private fun ScreenContent(
    state: ManualSelectionViewModel.State,
    onBackClicked: () -> Unit,
    onDeviceClick: (SupportedDevice) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            val focusRequester = remember { FocusRequester() }
            LaunchedEffect(Unit) { focusRequester.requestFocus() }

            var query by remember { mutableStateOf("") }
            val queryFlow = remember { MutableStateFlow(query) }

            TextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .focusRequester(focusRequester),
                placeholder = { Text(stringResource(R.string.manual_search_placeholder)) },
                leadingIcon = {
                    IconButton(onClick = onBackClicked) {
                        Icon(Icons.Outlined.ArrowBack, stringResource(R.string.back_description))
                    }
                },
                trailingIcon = {
                    IconButton(onClick = { query = "" }) {
                        Icon(Icons.Outlined.Clear, stringResource(R.string.clear))
                    }
                },
                singleLine = true,
            )

            val context = LocalContext.current
            var searchResult by remember { mutableStateOf(emptyList<SupportedDevices>()) }

            LaunchedEffect(query) {
                queryFlow.value = query
                queryFlow.debounce(INPUT_DELAY).collect {
                    searchResult = state.devices.applyQuery(query, context)
                }
            }

            if (searchResult.isNotEmpty()) {
                SearchResult(searchResult, onDeviceClick = onDeviceClick)
            }
        }
    }
}

@Composable
private fun Device(device: SupportedDevice, modifier: Modifier = Modifier) {
    val context = LocalContext.current

    Row(
        modifier
            .fillMaxWidth()
            .padding(
                start = PaddingDefault,
                top = PaddingDefault,
                end = PaddingNormal,
                bottom = PaddingDefault
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        DeviceImage(
            imageUri = device.img,
            Modifier.size(40.dp),
            name = device.type.getName(context),
        )
        Spacer(Modifier.width(PaddingDefault))
        Column {
            Text(
                stringResource(device.type.nameRes),
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                stringResource(device.family.nameRes),
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SearchResult(
    devices: List<SupportedDevices>,
    onDeviceClick: (SupportedDevice) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = PaddingNormal)) {
        items(devices) { device ->
            Device(
                device.supportedDevice,
                Modifier.clickable { onDeviceClick(device.supportedDevice) }
            )
        }
    }
}

private fun List<SupportedDevices>.applyQuery(
    query: String,
    context: Context
): List<SupportedDevices> {
    if (query.isBlank()) return emptyList()

    return this
        .map { device -> device.supportedDevice.type.getName(context) to device }
        .filter { typeDevicePair ->
            val family = typeDevicePair.second.supportedDevice.family.getName(context)
            query.trim().lowercase().let { query ->
                query in typeDevicePair.first.lowercase() || query in family.lowercase()
            }
        }
        .sortedBy { typeDevicePair -> typeDevicePair.first }
        .map { typeDevicePair -> typeDevicePair.second }
}

// region Previews

@Preview(showSystemUi = true)
@Composable
private fun Preview() {
    AppTheme {
        ScreenContent(
            state = ManualSelectionViewModel.State(supportedDevices),
            onDeviceClick = {},
            onBackClicked = {}
        )
    }
}

// endregion
