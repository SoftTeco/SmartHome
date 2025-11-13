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
            when (val result = thermometerRepository.getThermometerData(state.value.deviceMacAddress)) {
                is Result.Success -> handleThermometerDataSuccess(result.data)
                is Result.Error -> handleThermometerDataError(result.error.messageRes)
            }
        }
    }

    private fun handleThermometerDataSuccess(data: ThermometerData) {
        thermometer.value = data

        val (temperatureHistory, humidityHistory) = extractHistoryMaps(data.valuesHistory)
        fullTemperatureHistory.value = temperatureHistory
        fullHumidityHistory.value = humidityHistory

        val lastEntry = data.valuesHistory.lastOrNull() as? ThermometerValues.DataLYWSD03MMC
        val isDataFresh = isEntryFresh(lastEntry)
        val currentTemp = if (isDataFresh && lastEntry != null) {
            lastEntry.temperature
        } else {
            data.currentTemperature
        }
        val currentHum = if (isDataFresh && lastEntry != null) {
            lastEntry.humidity
        } else {
            data.currentHumidity
        }
        val hasCurrentData = currentTemp != 0.0 && currentHum != 0

        thermometer.value = data.copy(
            temperatureHistory = temperatureHistory,
            humidityHistory = humidityHistory,
            currentTemperature = currentTemp,
            currentHumidity = currentHum
        )

        val hasHistoryToDisplay = temperatureHistory.isNotEmpty() || humidityHistory.isNotEmpty()
        if (hasCurrentData || hasHistoryToDisplay) {
            loading.value = false
        }
    }

    private fun extractHistoryMaps(
        valuesHistory: List<ThermometerValues>
    ): Pair<Map<LocalDateTime, Float>, Map<LocalDateTime, Float>> {
        val temperatureHistory = mutableMapOf<LocalDateTime, Float>()
        val humidityHistory = mutableMapOf<LocalDateTime, Float>()

        valuesHistory.forEach {
            (it as ThermometerValues.DataLYWSD03MMC).let { data ->
                temperatureHistory[data.timestamp] = data.temperature.toFloat()
                humidityHistory[data.timestamp] = data.humidity.toFloat()
            }
        }

        return Pair(temperatureHistory, humidityHistory)
    }

    private fun isEntryFresh(lastEntry: ThermometerValues.DataLYWSD03MMC?): Boolean {
        if (lastEntry == null) return false
        val now = LocalDateTime.now()
        val ageInMinutes = Duration.between(lastEntry.timestamp, now).toMinutes()
        return ageInMinutes <= 1
    }

    private fun handleThermometerDataError(messageRes: Int) {
        loading.value = false
        snackbarController.showSnackbar(messageRes)
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
            when (val result = thermometerRepository.getCurrentMeasurement(state.value.deviceMacAddress)) {
                is Result.Success -> {
                    handleCurrentMeasurementSuccess(result.data, unit, measurementType)
                }
                is Result.Error -> {
                    snackbarController.showSnackbar(result.error.messageRes)
                }
            }
        }
    }

    private fun handleCurrentMeasurementSuccess(
        values: ThermometerValues,
        unit: TimeIntervalMenu,
        measurementType: MeasurementType
    ) {
        val data = values as ThermometerValues.DataLYWSD03MMC
        val currentHistory = getCurrentHistory(measurementType)
        val now = LocalDateTime.now()

        if (shouldUpdateHistory(currentHistory, now)) {
            val newMeasurementValue = getMeasurementValue(data, measurementType)
            updateHistoryAndThermometer(
                data = data,
                currentHistory = currentHistory,
                now = now,
                newMeasurementValue = newMeasurementValue,
                measurementType = measurementType,
                unit = unit
            )
        }

        if (loading.value) {
            loading.value = false
        }
    }

    private fun getCurrentHistory(measurementType: MeasurementType): Map<LocalDateTime, Float> {
        return when (measurementType) {
            MeasurementType.TEMPERATURE -> fullTemperatureHistory.value
            MeasurementType.HUMIDITY -> fullHumidityHistory.value
        }
    }

    private fun getMeasurementValue(
        data: ThermometerValues.DataLYWSD03MMC,
        measurementType: MeasurementType
    ): Number {
        return when (measurementType) {
            MeasurementType.TEMPERATURE -> data.temperature
            MeasurementType.HUMIDITY -> data.humidity
        }
    }

    private fun shouldUpdateHistory(currentHistory: Map<LocalDateTime, Float>, now: LocalDateTime): Boolean {
        val lastEntryTime = currentHistory.keys.maxOrNull()
        return lastEntryTime == null || Duration.between(lastEntryTime, now).toMinutes() >= MIN_VALUES_INTERVAL
    }

    private fun updateHistoryAndThermometer(
        data: ThermometerValues.DataLYWSD03MMC,
        currentHistory: Map<LocalDateTime, Float>,
        now: LocalDateTime,
        newMeasurementValue: Number,
        measurementType: MeasurementType,
        unit: TimeIntervalMenu
    ) {
        val updatedHistory = currentHistory.toMutableMap()
        updatedHistory[now] = newMeasurementValue.toFloat()

        val updatedThermometerData = createUpdatedThermometerData(
            data = data,
            updatedHistory = updatedHistory,
            measurementType = measurementType
        )

        thermometer.value = updatedThermometerData
        updateFullHistory(measurementType, updatedHistory)

        if (unit != TimeIntervalMenu.Minute) {
            updateThermometerHistoryByInterval(unit, measurementType)
        }
    }

    private fun createUpdatedThermometerData(
        data: ThermometerValues.DataLYWSD03MMC,
        updatedHistory: Map<LocalDateTime, Float>,
        measurementType: MeasurementType
    ): ThermometerData? {
        return when (measurementType) {
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
    }

    private fun updateFullHistory(
        measurementType: MeasurementType,
        updatedHistory: Map<LocalDateTime, Float>
    ) {
        when (measurementType) {
            MeasurementType.TEMPERATURE -> fullTemperatureHistory.value = updatedHistory
            MeasurementType.HUMIDITY -> fullHumidityHistory.value = updatedHistory
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
