package com.softteco.template.ui.feature.deviceprotocol.bluetooth

import android.bluetooth.BluetoothDevice
import androidx.compose.runtime.Immutable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.softteco.template.data.bluetooth.BluetoothHelper
import com.softteco.template.navigation.AppNavHost
import com.softteco.template.utils.protocol.DeviceConnectionStatus
import com.softteco.template.utils.protocol.getBluetoothDeviceName
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import no.nordicsemi.android.support.v18.scanner.ScanResult
import javax.inject.Inject

@HiltViewModel
class BluetoothViewModel @Inject constructor(
    private val bluetoothHelper: BluetoothHelper,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private var _bluetoothDevices = linkedMapOf<String, ScanResult>()
    private val _devices = MutableStateFlow<List<BluetoothDevice>>(emptyList())
    private val _deviceConnectionStatusList =
        MutableStateFlow<MutableList<DeviceConnectionStatus>>(
            mutableListOf()
        )
    private var filtered: Boolean = true

    val state = combine(
        _devices,
        _deviceConnectionStatusList
    ) { devices, deviceConnectionStatusList ->
        State(
            devices = devices,
            devicesConnectionStatusList = deviceConnectionStatusList,
            deviceName = checkNotNull(savedStateHandle.get<String>(AppNavHost.DEVICE_NAME_KEY))
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.Lazily,
        State()
    )

    fun initCallbacks() {
        onScanCallback {
            addScanResult(it)
        }

        onBluetoothModuleChangeStateCallback {
            if (!it) {
                clearScanResult()
            }
        }
    }

    fun registerReceiver() {
        bluetoothHelper.registerReceiver()
    }

    fun unregisterReceiver() {
        bluetoothHelper.unregisterReceiver()
    }

    fun startScanIfHasPermissions() {
        bluetoothHelper.startScanIfHasPermissions()
    }

    suspend fun provideConnectionToDevice(bluetoothDevice: BluetoothDevice) {
        bluetoothHelper.provideConnectionToDevice(bluetoothDevice)
    }

    private fun addScanResult(scanResult: ScanResult) {
        _bluetoothDevices[scanResult.device.address] = scanResult
        emitDevices()
    }

    private fun clearScanResult() {
        _devices.value = emptyList()
    }

    private fun onScanCallback(onScanResult: (scanResult: ScanResult) -> Unit) {
        bluetoothHelper.onScanCallback(onScanResult)
    }

    private fun onBluetoothModuleChangeStateCallback(onBluetoothModuleChangeState: (ifTurnOn: Boolean) -> Unit) {
        bluetoothHelper.onBluetoothModuleChangeStateCallback(onBluetoothModuleChangeState)
    }

    fun getDeviceConnectionStatusList() {
        viewModelScope.launch {
            bluetoothHelper.getObservableDeviceConnectionStatusList().collect { statusList ->
                _deviceConnectionStatusList.value = statusList.values.toMutableList()
            }
        }
    }

    private fun emitDevices() {
        _devices.value = _bluetoothDevices
            .map { it.value.device }
            .filter { device ->
                if (filtered) getBluetoothDeviceName(device) == state.value.deviceName else true
            }
    }

    @Immutable
    data class State(
        val devices: List<BluetoothDevice> = emptyList(),
        val devicesConnectionStatusList: List<DeviceConnectionStatus> = emptyList(),
        val dismissSnackBar: () -> Unit = {},
        val deviceName: String = ""
    )
}
