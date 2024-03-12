package com.softteco.template.ui.feature.chart

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.softteco.template.data.bluetooth.BluetoothHelper
import com.softteco.template.data.measurement.entity.MeasurementDevice
import com.softteco.template.data.measurement.usecase.MeasurementGetUseCase
import com.softteco.template.ui.components.snackBar.SnackBarState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ChartViewModel @Inject constructor(
    private val bluetoothHelper: BluetoothHelper,
    private val measurementGetUseCase: MeasurementGetUseCase,
) : ViewModel() {

    private var snackBarState = MutableStateFlow(SnackBarState())
    private var measurements = MutableStateFlow<List<MeasurementDevice>>(emptyList())

    val state = combine(
        snackBarState,
        measurements
    ) { snackBar, measurements ->
        State(
            measurements = measurements,
            snackBar = snackBar,
            dismissSnackBar = { snackBarState.value = SnackBarState() }
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.Lazily,
        State()
    )

    fun onDeviceResultCallback(onDeviceResult: (macAddress: String) -> Unit) {
        bluetoothHelper.onDeviceResultCallback(onDeviceResult)
    }

    suspend fun getMeasurements(macAddressOfDevice: String) {
        measurements.value = measurementGetUseCase.execute(macAddressOfDevice).first().map { it.toEntity() }
    }

    @Immutable
    data class State(
        val measurements: List<MeasurementDevice> = emptyList(),
        val snackBar: SnackBarState = SnackBarState(),
        val dismissSnackBar: () -> Unit = {}
    )
}
