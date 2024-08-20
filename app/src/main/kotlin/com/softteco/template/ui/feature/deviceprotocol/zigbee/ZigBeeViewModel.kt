package com.softteco.template.ui.feature.deviceprotocol.zigbee

import androidx.compose.runtime.Immutable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.softteco.template.data.zigbee.ZigbeeHelper
import com.softteco.template.navigation.AppNavHost
import com.softteco.template.utils.ZigbeeDevice
import com.softteco.template.utils.ZigbeeTopic
import com.softteco.template.utils.protocol.DeviceConnectionStatus
import com.softteco.template.utils.protocol.getZigBeeDeviceName
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ZigBeeViewModel @Inject constructor(
    private val zigbeeHelper: ZigbeeHelper,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private var _zigbeeDevices = linkedMapOf<String, ZigbeeDevice>()
    private val _devices = MutableStateFlow<List<ZigbeeDevice>>(emptyList())
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

    init {
        initCallbacks()
        zigbeeHelper.connect(ZigbeeTopic.ZIGBEE_DEVICE_TOPIC.value)
        getDeviceConnectionStatusList()
    }

    suspend fun provideConnectionToDevice(deviceName: String) {
        zigbeeHelper.provideConnectionToDevice(ZigbeeTopic.ZIGBEE_DATA_TOPIC.value + deviceName)
    }

    private fun getDeviceConnectionStatusList() {
        viewModelScope.launch {
            zigbeeHelper.getObservableDeviceConnectionStatusList().collect { statusList ->
                _deviceConnectionStatusList.value = statusList.values.toMutableList()
            }
        }
    }

    private fun initCallbacks() {
        onScanCallback {
            addScanResult(it)
        }
    }

    private fun onScanCallback(onScanResult: (device: ZigbeeDevice) -> Unit) {
        zigbeeHelper.onScanCallback(onScanResult)
    }

    private fun addScanResult(device: ZigbeeDevice) {
        _zigbeeDevices[device.ieeeAddress] = device
        emitDevices()
    }

    private fun emitDevices() {
        _devices.value = _zigbeeDevices
            .map { it.value }
            .filter { device ->
                if (filtered) getZigBeeDeviceName(device) == state.value.deviceName else true
            }
    }

    @Immutable
    data class State(
        val devices: List<ZigbeeDevice> = emptyList(),
        val devicesConnectionStatusList: List<DeviceConnectionStatus> = emptyList(),
        val dismissSnackBar: () -> Unit = {},
        val deviceName: String = ""
    )
}
