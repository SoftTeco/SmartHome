package com.softteco.template.ui.feature.chart

import android.annotation.SuppressLint
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.softteco.template.data.bluetooth.BluetoothHelper
import com.softteco.template.data.bluetooth.entity.BluetoothDeviceData
import com.softteco.template.ui.components.SnackBarState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChartViewModel @Inject constructor(
    private val bluetoothHelper: BluetoothHelper
) : ViewModel() {

    private var snackBarState = MutableStateFlow(SnackBarState())
    private var dataFromBluetoothDevice = MutableStateFlow<BluetoothDeviceData>(BluetoothDeviceData.Default)

    val state = combine(
        snackBarState,
        dataFromBluetoothDevice
    ) { snackBar, dataFromBluetoothDevice ->
        State(
            dataFromBluetoothDevice = dataFromBluetoothDevice,
            snackBar = snackBar,
            dismissSnackBar = { snackBarState.value = SnackBarState() }
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.Lazily,
        State()
    )

    fun onDeviceResultCallback(onDeviceResult: (bluetoothDeviceData: BluetoothDeviceData) -> Unit) {
        bluetoothHelper.onDeviceResultCallback(onDeviceResult)
    }

    @SuppressLint("MissingPermission")
    fun bluetoothData(bluetoothDeviceData: BluetoothDeviceData) {
        viewModelScope.launch {
            dataFromBluetoothDevice.value = bluetoothDeviceData
        }
    }

    @Immutable
    data class State(
        val dataFromBluetoothDevice: BluetoothDeviceData = BluetoothDeviceData.Default,
        val snackBar: SnackBarState = SnackBarState(),
        val dismissSnackBar: () -> Unit = {}
    )
}
