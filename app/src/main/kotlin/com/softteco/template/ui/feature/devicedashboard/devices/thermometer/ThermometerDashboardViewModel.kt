package com.softteco.template.ui.feature.devicedashboard.devices.thermometer

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.common.data.ExtraStore
import com.softteco.template.R
import com.softteco.template.data.base.error.Result
import com.softteco.template.data.bluetooth.BluetoothHelper
import com.softteco.template.data.device.ProtocolType
import com.softteco.template.data.device.ThermometerData
import com.softteco.template.data.device.ThermometerRepository
import com.softteco.template.data.device.ThermometerValues
import com.softteco.template.data.zigbee.ZigbeeHelper
import com.softteco.template.navigation.AppNavHost
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
    private val bluetoothHelper: BluetoothHelper,
    private val zigbeeHelper: ZigbeeHelper,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val loading = MutableStateFlow(true)

    private val fullTemperatureHistory = MutableStateFlow<Map<LocalDateTime, Float>>(
        emptyMap()
    )

    private val fullHumidityHistory = MutableStateFlow<Map<LocalDateTime, Float>>(
        emptyMap()
    )

    private val thermometer = MutableStateFlow<ThermometerData?>(null)

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
            loading = loading,
            deviceProtocol = checkNotNull(savedStateHandle.get<String>(AppNavHost.DEVICE_PROTOCOL)),
            deviceMacAddress = checkNotNull(savedStateHandle.get<String>(AppNavHost.DEVICE_MAC_ADDRESS))
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.Lazily,
        State()
    )

    fun getThermometerHistory() {
        loading.value = true
        viewModelScope.launch(appDispatchers.io) {
            val result = thermometerRepository.getThermometerData(state.value.deviceMacAddress)
            when (result) {
                is Result.Success -> {
                    thermometer.value = result.data
                    val temperatureHistory = emptyMap<LocalDateTime, Float>().toMutableMap()
                    val humidityHistory = emptyMap<LocalDateTime, Float>().toMutableMap()

                    result.data.valuesHistory.forEach {
                        (it as ThermometerValues.DataLYWSD03MMC).let { data ->
                            temperatureHistory[data.timestamp] = data.temperature.toFloat()
                            humidityHistory[data.timestamp] = data.humidity.toFloat()
                        }
                    }

                    fullTemperatureHistory.value = temperatureHistory
                    fullHumidityHistory.value = humidityHistory

                    val hasCurrentData = result.data.currentTemperature != 0.0 && result.data.currentHumidity != 0
                    val lastEntry = result.data.valuesHistory.lastOrNull() as? ThermometerValues.DataLYWSD03MMC
                    val isDataFresh = if (lastEntry != null) {
                        val now = LocalDateTime.now()
                        val ageInMinutes = Duration.between(lastEntry.timestamp, now).toMinutes()
                        ageInMinutes <= 1
                    } else {
                        false
                    }

                    val displayTemp = if (result.data.currentTemperature == 0.0 && isDataFresh && lastEntry != null) {
                        lastEntry.temperature
                    } else {
                        result.data.currentTemperature
                    }
                    val displayHum = if (result.data.currentHumidity == 0 && isDataFresh && lastEntry != null) {
                        lastEntry.humidity
                    } else {
                        result.data.currentHumidity
                    }

                    thermometer.value = result.data.copy(
                        temperatureHistory = temperatureHistory,
                        humidityHistory = humidityHistory,
                        currentTemperature = displayTemp,
                        currentHumidity = displayHum
                    )

                    if (hasCurrentData || isDataFresh) {
                        loading.value = false
                    }
                }

                is Result.Error -> {
                    loading.value = false
                    snackbarController.showSnackbar(result.error.messageRes)
                }
            }
        }
    }

    fun onDeviceDataReceived(callback: () -> Unit) {
        val protocolType = ProtocolType.fromString(state.value.deviceProtocol)
        when (protocolType) {
            ProtocolType.ZIGBEE -> {
                zigbeeHelper.onDeviceDataReceived(callback)
            }

            ProtocolType.BLUETOOTH -> {
                bluetoothHelper.onDeviceDataReceived(callback)
            }

            else -> {}
        }
    }

    fun updateThermometerHistoryByInterval(unit: TimeIntervalMenu, type: MeasurementType) {
        val fullHistory = when (type) {
            MeasurementType.TEMPERATURE -> fullTemperatureHistory.value
            MeasurementType.HUMIDITY -> fullHumidityHistory.value
        }

        val updatedHistory = if (unit != TimeIntervalMenu.Minute) {
            val aggregated = aggregateByInterval(fullHistory, unit.chronoUnit)
            aggregated
        } else {
            fullHistory
        }
        
        thermometer.value = when (type) {
            MeasurementType.TEMPERATURE -> {
                thermometer.value?.copy(temperatureHistory = updatedHistory)
            }
            MeasurementType.HUMIDITY -> {
                thermometer.value?.copy(humidityHistory = updatedHistory)
            }
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
            val result = thermometerRepository.getCurrentMeasurement(state.value.deviceMacAddress)

            when (result) {
                is Result.Success -> {
                    val data = result.data as ThermometerValues.DataLYWSD03MMC
                    val newMeasurementValue = when (measurementType) {
                        MeasurementType.TEMPERATURE -> data.temperature
                        MeasurementType.HUMIDITY -> data.humidity
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
                            MeasurementType.TEMPERATURE -> thermometer.value?.copy(
                                temperatureHistory = updatedHistory,
                                currentTemperature = data.temperature,
                                currentHumidity = data.humidity
                            )

                            MeasurementType.HUMIDITY -> thermometer.value?.copy(
                                humidityHistory = updatedHistory,
                                currentTemperature = data.temperature,
                                currentHumidity = data.humidity
                            )
                        }

                        thermometer.value = updatedThermometerData

                        when (measurementType) {
                            MeasurementType.TEMPERATURE -> {
                                fullTemperatureHistory.value = updatedHistory
                            }
                            MeasurementType.HUMIDITY -> {
                                fullHumidityHistory.value = updatedHistory
                            }
                        }

                        if (unit != TimeIntervalMenu.Minute) {
                            updateThermometerHistoryByInterval(unit, measurementType)
                        }
                    }
                    
                    if (loading.value) {
                        loading.value = false
                    }
                }

                is Result.Error -> {
                    snackbarController.showSnackbar(result.error.messageRes)
                }
            }
        }
    }

    @Immutable
    data class State(
        val thermometer: ThermometerData? = null,
        val bottomAxisValueFormatter: CartesianValueFormatter = CartesianValueFormatter { x, chartValues, _ ->
            val dateTime = chartValues.model.extraStore[xToDateMapKeyLocalDateTime][x]
            if (dateTime != null) {
                dateTime.format(TimeIntervalMenu.Minute.dateTimeFormatter)
            } else {
                ""
            }
        },
        val loading: Boolean = false,
        val deviceProtocol: String = "",
        val deviceMacAddress: String = ""
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
