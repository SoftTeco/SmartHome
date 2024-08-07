package com.softteco.template.ui.feature.devicedashboard.devices.thermometer

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.common.data.ExtraStore
import com.softteco.template.R
import com.softteco.template.data.base.error.Result
import com.softteco.template.data.bluetooth.BluetoothHelper
import com.softteco.template.data.device.ThermometerData
import com.softteco.template.data.device.ThermometerRepository
import com.softteco.template.data.device.ThermometerValues
import com.softteco.template.ui.components.snackbar.SnackbarController
import com.softteco.template.utils.AppDispatchers
import com.softteco.template.utils.ChartUtils.aggregateByInterval
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import javax.inject.Inject

val xToDateMapKeyLocalDateTime = ExtraStore.Key<Map<Float, LocalDateTime>>()
const val ITEM_PLACER_COUNT = 4
const val MIN_VALUES_INTERVAL = 1

@HiltViewModel
class ThermometerDashboardViewModel @Inject constructor(
    private val thermometerRepository: ThermometerRepository,
    private val snackbarController: SnackbarController,
    private val appDispatchers: AppDispatchers,
    private val bluetoothHelper: BluetoothHelper
) : ViewModel() {
    private val loading = MutableStateFlow(false)

    private val fullTemperatureHistory = MutableStateFlow<Map<LocalDateTime, Float>>(
        emptyMap()
    )

    private val fullHumidityHistory = MutableStateFlow<Map<LocalDateTime, Float>>(
        emptyMap()
    )

    private val thermometer = MutableStateFlow(ThermometerData("", "", ""))

    private val bottomAxisValueFormatter = MutableStateFlow(
        CartesianValueFormatter { x, chartValues, _ ->
            val dateTime = chartValues.model.extraStore[xToDateMapKeyLocalDateTime][x]
            if (dateTime != null) {
                dateTime.format(TimeIntervalMenu.Minute.dateTimeFormatter)
            } else {
                ""
            }
        }
    )

    val state = combine(
        thermometer,
        bottomAxisValueFormatter,
        loading
    ) { thermometer, formatter, loading ->
        State(
            thermometer = thermometer,
            bottomAxisValueFormatter = formatter,
            loading = loading
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.Lazily,
        State()
    )

    init {
        getThermometerHistory()
    }

    private fun getThermometerHistory() {
        loading.value = true
        viewModelScope.launch(appDispatchers.io) {
            when (
                val result =
                    thermometerRepository.getThermometerData(bluetoothHelper.getCurrentlyViewedBluetoothDeviceAddress())
            ) {
                is Result.Success -> {
                    thermometer.value = result.data
                    val temperatureHistory = emptyMap<LocalDateTime, Float>().toMutableMap()
                    val humidityHistory = emptyMap<LocalDateTime, Float>().toMutableMap()
                    result.data.valuesHistory.forEach {
                        (it as ThermometerValues.DataLYWSD03MMC).let {
                            temperatureHistory[it.timestamp] = it.temperature.toFloat()
                            humidityHistory[it.timestamp] = it.humidity.toFloat()
                        }
                    }
                    thermometer.value.temperatureHistory = temperatureHistory
                    thermometer.value.humidityHistory = humidityHistory
                    fullTemperatureHistory.value = temperatureHistory
                    fullHumidityHistory.value = humidityHistory
                    loading.value = false
                }

                is Result.Error -> {
                    loading.value = false
                    snackbarController.showSnackbar(result.error.messageRes)
                }
            }
        }
    }

    fun onDeviceResultCallback(onDeviceResult: () -> Unit) {
        bluetoothHelper.onDeviceResultCallback(onDeviceResult)
    }

    fun updateThermometerHistoryByInterval(unit: TimeIntervalMenu, type: MeasurementType) {
        val fullHistory = when (type) {
            MeasurementType.TEMPERATURE -> fullTemperatureHistory.value
            MeasurementType.HUMIDITY -> fullHumidityHistory.value
        }

        val updatedHistory = if (unit != TimeIntervalMenu.Minute) {
            aggregateByInterval(fullHistory, unit.chronoUnit)
        } else {
            fullHistory
        }
        thermometer.value = when (type) {
            MeasurementType.TEMPERATURE -> thermometer.value.copy(temperatureHistory = updatedHistory)
            MeasurementType.HUMIDITY -> thermometer.value.copy(humidityHistory = updatedHistory)
        }

        bottomAxisValueFormatter.value = CartesianValueFormatter { x, chartValues, _ ->
            val dateTime = chartValues.model.extraStore[xToDateMapKeyLocalDateTime][x]
            if (dateTime != null) {
                dateTime.format(unit.dateTimeFormatter)
            } else {
                ""
            }
        }
    }

    fun getCurrentMeasurement(unit: TimeIntervalMenu, measurementType: MeasurementType) {
        viewModelScope.launch(appDispatchers.io) {
            when (
                val result =
                    thermometerRepository.getCurrentMeasurement(
                        bluetoothHelper.getCurrentlyViewedBluetoothDeviceAddress()
                    )
            ) {
                is Result.Success -> {
                    val newMeasurementValue = when (measurementType) {
                        MeasurementType.TEMPERATURE -> (result.data as ThermometerValues.DataLYWSD03MMC).temperature
                        MeasurementType.HUMIDITY -> (result.data as ThermometerValues.DataLYWSD03MMC).humidity
                    }
                    val currentHistory = when (measurementType) {
                        MeasurementType.TEMPERATURE -> fullTemperatureHistory.value
                        MeasurementType.HUMIDITY -> fullHumidityHistory.value
                    }

                    val lastEntryTime = currentHistory.keys.maxOrNull()
                    val now = LocalDateTime.now()

                    if (lastEntryTime == null ||
                        Duration.between(lastEntryTime, now).toMinutes() >= MIN_VALUES_INTERVAL
                    ) {
                        val updatedHistory = currentHistory.toMutableMap()

                        updatedHistory[now] = newMeasurementValue.toFloat()

                        val updatedThermometerData = when (measurementType) {
                            MeasurementType.TEMPERATURE -> thermometer.value.copy(
                                temperatureHistory = updatedHistory,
                                currentTemperature = result.data.temperature,
                                currentHumidity = result.data.humidity
                            )

                            MeasurementType.HUMIDITY -> thermometer.value.copy(
                                humidityHistory = updatedHistory,
                                currentTemperature = result.data.temperature,
                                currentHumidity = result.data.humidity
                            )
                        }

                        thermometer.value = updatedThermometerData

                        when (measurementType) {
                            MeasurementType.TEMPERATURE ->
                                fullTemperatureHistory.value =
                                    updatedHistory

                            MeasurementType.HUMIDITY -> fullHumidityHistory.value = updatedHistory
                        }

                        if (unit != TimeIntervalMenu.Minute) {
                            updateThermometerHistoryByInterval(unit, measurementType)
                        }
                    }
                    loading.value = false
                }

                is Result.Error -> {
                    snackbarController.showSnackbar(result.error.messageRes)
                }
            }
        }
    }

    @Immutable
    data class State(
        val thermometer: ThermometerData = ThermometerData("", "", ""),
        val bottomAxisValueFormatter: CartesianValueFormatter = CartesianValueFormatter { x, chartValues, _ ->
            val dateTime = chartValues.model.extraStore[xToDateMapKeyLocalDateTime][x]
            if (dateTime != null) {
                dateTime.format(TimeIntervalMenu.Minute.dateTimeFormatter)
            } else {
                ""
            }
        },
        val loading: Boolean = false,
    )

    enum class TimeIntervalMenu(
        @StringRes val labelResourceID: Int,
        val dateTimeFormatter: DateTimeFormatter,
        val chronoUnit: ChronoUnit
    ) {
        Minute(R.string.minute, DateTimeFormatter.ofPattern("d MMM, HH:mm"), ChronoUnit.MINUTES),
        Hour(R.string.hour, DateTimeFormatter.ofPattern("d MMM, HH"), ChronoUnit.HOURS),
        Day(R.string.day, DateTimeFormatter.ofPattern("d MMM yy"), ChronoUnit.DAYS),
        Month(R.string.month, DateTimeFormatter.ofPattern("MMM yy"), ChronoUnit.MONTHS)
    }

    enum class MeasurementType {
        TEMPERATURE,
        HUMIDITY
    }
}
