package com.softteco.template.ui.feature.deviceDashboard.devices.robotVacuum

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.softteco.template.ui.components.DeviceDashboardTopAppBar
import com.softteco.template.ui.theme.AppTheme
import com.softteco.template.ui.theme.Dimens
import java.util.UUID

@Composable
fun RobotVacuumDashboardScreen(
    onSettingsClick: (deviceId: String) -> Unit,
    onBackClicked: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RobotVacuumDashboardViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    ScreenContent(
        state,
        onSettingsClick = onSettingsClick,
        modifier = modifier,
        onBackClicked = onBackClicked,
    )
}

@Composable
private fun ScreenContent(
    state: RobotVacuumDashboardViewModel.State,
    onSettingsClick: (deviceId: String) -> Unit,
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
        DeviceDashboardTopAppBar(
            state.deviceId,
            onSettingsClick = { onSettingsClick(state.deviceId) },
            modifier = Modifier.fillMaxWidth(),
            onBackClicked = onBackClicked
        )
        Column(
            modifier = Modifier.padding(Dimens.PaddingNormal),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "RobotVacuumDashboard",
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
            state = RobotVacuumDashboardViewModel.State(UUID.randomUUID().toString()),
            onSettingsClick = {},
            onBackClicked = {},
        )
    }
}
