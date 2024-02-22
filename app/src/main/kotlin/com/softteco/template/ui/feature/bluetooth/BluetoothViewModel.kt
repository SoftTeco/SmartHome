package com.softteco.template.ui.feature.bluetooth

import android.bluetooth.BluetoothDevice
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.softteco.template.data.bluetooth.BluetoothHelper
import com.softteco.template.data.bluetooth.entity.BluetoothDeviceConnectionStatus
import com.softteco.template.ui.components.SnackBarState
import com.softteco.template.utils.bluetooth.getBluetoothDeviceName
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import no.nordicsemi.android.support.v18.scanner.ScanResult
import javax.inject.Inject

@HiltViewModel
class BluetoothViewModel @Inject constructor(
    private val bluetoothHelper: BluetoothHelper
) : ViewModel() {

    private val _snackBarState = MutableStateFlow(SnackBarState())
    private var _bluetoothDevices = linkedMapOf<String, ScanResult>()
    private val _devices = MutableStateFlow<List<BluetoothDevice>>(emptyList())
    private val _deviceConnectionStatusList =
        MutableStateFlow<MutableList<BluetoothDeviceConnectionStatus>>(
            mutableListOf()
        )
    private val mutex = Mutex()
    private var filtered: Boolean = true
    var filteredName: String = ""

    val state = combine(
        _snackBarState,
        _devices,
        _deviceConnectionStatusList
    ) { snackBar, devices, deviceConnectionStatusList ->
        State(
            devices = devices,
            snackBar = snackBar,
            devicesConnectionStatusList = deviceConnectionStatusList,
            dismissSnackBar = { _snackBarState.value = SnackBarState() }
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.Lazily,
        State()
    )

    fun addScanResult(scanResult: ScanResult) {
        _bluetoothDevices[scanResult.device.address] = scanResult
        emitDevices()
    }

    fun disconnectFromDevice() {
//        bluetoothHelper.disconnectFromDevice()
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

    fun provideConnectionToDevice(bluetoothDevice: BluetoothDevice) {
        bluetoothHelper.provideConnectionToDevice(bluetoothDevice)
    }

    fun onScanCallback(onScanResult: (scanResult: ScanResult) -> Unit) {
        bluetoothHelper.onScanCallback(onScanResult)
    }

    fun onConnectCallback(onConnect: () -> Unit) {
        bluetoothHelper.onConnectCallback(onConnect)
    }

    fun onDisconnectCallback(onDisconnect: () -> Unit) {
        bluetoothHelper.onDisconnectCallback(onDisconnect)
    }

    fun emitDeviceConnectionStatusList() {
        _deviceConnectionStatusList.value =
            bluetoothHelper.getDeviceConnectionStatusList().values.toMutableList()
    }

    fun setFiltered(filtered: Boolean) {
        this.filtered = filtered
        emitDevices()
    }

    private fun emitDevices() {
        viewModelScope.launch(Dispatchers.IO) {
            mutex.withLock {
        _devices.value =
            _bluetoothDevices.toList().map { it.second.device }
                .let { devices ->
                    if (filtered) devices.filter { getBluetoothDeviceName(it) == filteredName } else devices
                }
            }
        }
    }

    @Immutable
    data class State(
        val devices: List<BluetoothDevice> = emptyList(),
        val devicesConnectionStatusList: List<BluetoothDeviceConnectionStatus> = emptyList(),
        val snackBar: SnackBarState = SnackBarState(),
        val dismissSnackBar: () -> Unit = {}
    )
}
