package com.softteco.template.ui.feature.devicedashboard.devices.thermometer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SevereCold
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.softteco.template.R
import com.softteco.template.data.device.ThermometerData
import com.softteco.template.ui.components.DashboardValueBlock
import com.softteco.template.ui.components.DateTimeChart
import com.softteco.template.ui.components.DeviceDashboardTopAppBar
import com.softteco.template.ui.feature.devicedashboard.devices.thermometer.ThermometerDashboardViewModel.MeasurementType
import com.softteco.template.ui.feature.devicedashboard.devices.thermometer.ThermometerDashboardViewModel.MeasurementType.HUMIDITY
import com.softteco.template.ui.feature.devicedashboard.devices.thermometer.ThermometerDashboardViewModel.MeasurementType.TEMPERATURE
import com.softteco.template.ui.feature.devicedashboard.devices.thermometer.ThermometerDashboardViewModel.TimeIntervalMenu
import com.softteco.template.ui.theme.AppTheme
import com.softteco.template.ui.theme.Dimens
import kotlinx.coroutines.launch

@Composable
fun ThermometerDashboardScreen(
    onSettingsClick: (deviceId: String) -> Unit,
    onBackClicked: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ThermometerDashboardViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()

    ScreenContent(
        state,
        updateCurrentMeasurement = { unit, type -> viewModel.getCurrentMeasurement(unit, type) },
        updateThermometerHistoryByInterval = { unit, type -> viewModel.updateThermometerHistoryByInterval(unit, type) },
        updateCharts = { callback -> viewModel.onDeviceResultCallback(callback) },
        onSettingsClick = onSettingsClick,
        modifier = modifier,
        onBackClicked = onBackClicked
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScreenContent(
    state: ThermometerDashboardViewModel.State,
    onSettingsClick: (deviceId: String) -> Unit,
    updateCurrentMeasurement: (unit: TimeIntervalMenu, type: MeasurementType) -> Unit,
    updateThermometerHistoryByInterval: (unit: TimeIntervalMenu, type: MeasurementType) -> Unit,
    updateCharts: (callback: () -> Unit) -> Unit,
    modifier: Modifier = Modifier,
    onBackClicked: () -> Unit
) {
    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.background)
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(Dimens.PaddingDefault),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        var chartType by remember { mutableStateOf(TEMPERATURE) }
        var previousChartType by remember { mutableStateOf(chartType) }
        var timeIntervalMenu by rememberSaveable { mutableStateOf(TimeIntervalMenu.Minute) }
        val scope = rememberCoroutineScope()

        LaunchedEffect(Unit) {
            updateCharts {
                scope.launch {
                    updateCurrentMeasurement(timeIntervalMenu, chartType)
                    if (previousChartType != chartType) {
                        updateThermometerHistoryByInterval(timeIntervalMenu, chartType)
                        previousChartType = chartType
                    }
                }
            }
        }
        DeviceDashboardTopAppBar(
            state.thermometer.deviceName,
            onSettingsClick = { onSettingsClick(state.thermometer.deviceId.toString()) },
            modifier = Modifier.fillMaxWidth(),
            onBackClicked = onBackClicked
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            DashboardValueBlock(
                value = state.thermometer.currentTemperature,
                valueName = stringResource(R.string.temperature),
                measurementUnit = stringResource(R.string.degrees_celsius_icon),
                icon = Icons.Filled.SevereCold,
                modifier = Modifier
                    .padding(start = Dimens.PaddingDefault, end = Dimens.PaddingSmall)
                    .weight(1f),
                onClick = {
                    chartType = TEMPERATURE
                    updateCurrentMeasurement(timeIntervalMenu, chartType)
                    if (previousChartType != chartType) {
                        updateThermometerHistoryByInterval(timeIntervalMenu, chartType)
                        previousChartType = chartType
                    }
                }
            )

            DashboardValueBlock(
                value = state.thermometer.currentHumidity,
                valueName = stringResource(R.string.humidity),
                measurementUnit = stringResource(R.string.percent_icon),
                icon = Icons.Outlined.WaterDrop,
                modifier = Modifier
                    .padding(start = Dimens.PaddingSmall, end = Dimens.PaddingDefault)
                    .weight(1f),
                onClick = {
                    chartType = HUMIDITY
                    updateCurrentMeasurement(timeIntervalMenu, chartType)
                    if (previousChartType != chartType) {
                        updateThermometerHistoryByInterval(timeIntervalMenu, chartType)
                        previousChartType = chartType
                    }
                }
            )
        }

        SingleChoiceSegmentedButtonRow(
            Modifier
                .padding(horizontal = Dimens.PaddingDefault)
                .fillMaxWidth()
        ) {
            TimeIntervalMenu.entries.forEachIndexed { index, segmentUIFramework ->
                SegmentedButton(
                    selected = timeIntervalMenu == segmentUIFramework,
                    onClick = {
                        timeIntervalMenu = segmentUIFramework
                        updateThermometerHistoryByInterval(timeIntervalMenu, chartType)
                    },
                    shape = SegmentedButtonDefaults.itemShape(index, TimeIntervalMenu.entries.size),
                ) {
                    Text(stringResource(segmentUIFramework.labelResourceID))
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Chart(state, chartType, timeIntervalMenu)
        }
    }
}

@Composable
private fun Chart(
    state: ThermometerDashboardViewModel.State,
    chartType: MeasurementType,
    timeIntervalMenu: TimeIntervalMenu,
) {
    ElevatedCard(
        modifier = Modifier.padding(horizontal = Dimens.PaddingDefault)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            if (state.loading) {
                CircularProgressIndicator(Modifier.size(Dimens.PaddingNormal))
            }
            if (chartType == TEMPERATURE) {
                DateTimeChart(
                    values = state.thermometer.temperatureHistory,
                    bottomAxisValueFormatter = state.bottomAxisValueFormatter,
                    timeUnit = timeIntervalMenu.chronoUnit,
                    yAxisTitle = R.string.temperature,
                )
            } else {
                DateTimeChart(
                    values = state.thermometer.humidityHistory,
                    bottomAxisValueFormatter = state.bottomAxisValueFormatter,
                    timeUnit = timeIntervalMenu.chronoUnit,
                    yAxisTitle = R.string.humidity,
                )
            }
        }
    }
}

@Preview
@Composable
private fun Preview() {
    AppTheme {
        ScreenContent(
            state = ThermometerDashboardViewModel.State(thermometer = ThermometerData()),
            updateCurrentMeasurement = { _, _ -> },
            onSettingsClick = {},
            updateThermometerHistoryByInterval = { _, _ -> },
            onBackClicked = {},
            updateCharts = { _ -> }
        )
    }
}
