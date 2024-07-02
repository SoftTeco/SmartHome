package com.softteco.template.ui.feature.devicedashboard.devicesettings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.softteco.template.R
import com.softteco.template.ui.theme.AppTheme
import com.softteco.template.ui.theme.Dimens
import java.util.UUID

@Composable
fun DeviceSettingsScreen(
    onBackClicked: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DeviceSettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    ScreenContent(
        state = state,
        modifier = modifier,
        onBackClicked = onBackClicked,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScreenContent(
    state: DeviceSettingsViewModel.State,
    modifier: Modifier = Modifier,
    onBackClicked: () -> Unit,
) {
    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.background)
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(Dimens.PaddingExtraLarge),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TopAppBar(
            title = { Text(stringResource(R.string.settings)) },
            navigationIcon = {
                IconButton(onClick = onBackClicked) {
                    Icon(Icons.Outlined.ArrowBack, stringResource(R.string.back_description))
                }
            },
            modifier = Modifier.statusBarsPadding()
        )
        Column(
            modifier = Modifier.padding(Dimens.PaddingNormal),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "DeviceSettings ${state.deviceId}",
                style = TextStyle(textAlign = TextAlign.Center),
            )
        }
    }
}

@Preview
@Composable
private fun Preview() {
    AppTheme {
        ScreenContent(
            state = DeviceSettingsViewModel.State(UUID.randomUUID().toString()),
            onBackClicked = {},
        )
    }
}
