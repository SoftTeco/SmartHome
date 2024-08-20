package com.softteco.template.ui.feature.home

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.softteco.template.data.base.error.Result
import com.softteco.template.data.bluetooth.BluetoothHelper
import com.softteco.template.data.device.Device
import com.softteco.template.data.device.ProtocolType
import com.softteco.template.data.device.ThermometerRepository
import com.softteco.template.data.zigbee.ZigbeeHelper
import com.softteco.template.navigation.Screen
import com.softteco.template.utils.protocol.DeviceConnectionStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val thermometerRepository: ThermometerRepository,
    private val bluetoothHelper: BluetoothHelper,
    private val zigbeeHelper: ZigbeeHelper
) : ViewModel() {

    private val _navDestination = MutableSharedFlow<Screen>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val navDestination = _navDestination.asSharedFlow()

    private val _devices = MutableStateFlow<List<Device>>(emptyList())

    private val _deviceConnectionStatusList =
        MutableStateFlow<MutableList<DeviceConnectionStatus>>(
            mutableListOf()
        )

    val state = combine(
        _devices,
        _deviceConnectionStatusList
    ) { devices, deviceConnectionStatusList ->
        State(
            devices = devices,
            devicesConnectionStatusList = deviceConnectionStatusList,
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.Lazily,
        State()
    )

    fun getDeviceConnectionStatusList() {
        combine(
            zigbeeHelper.getObservableDeviceConnectionStatusList(),
            bluetoothHelper.getObservableDeviceConnectionStatusList()
        ) { zigbeeStatusList, bluetoothStatusList ->
            val combinedList = mutableListOf<DeviceConnectionStatus>().apply {
                addAll(zigbeeStatusList.values)
                addAll(bluetoothStatusList.values)
            }
            _deviceConnectionStatusList.value = combinedList
        }.launchIn(viewModelScope)
    }

    fun getDevices() {
        runBlocking {
            withContext(Dispatchers.IO) {
                when (val result = thermometerRepository.getDevices()) {
                    is Result.Success -> {
                        _devices.value = result.data
                    }

                    is Result.Error -> {}
                }
            }
        }
    }

    fun performDeviceClick(
        onDeviceClick: (Device) -> Unit,
        devicesConnectionStatusList: List<DeviceConnectionStatus>,
        device: Device
    ) {
        if(isDeviceConnected(device, devicesConnectionStatusList)) {
            onDeviceClick(device)
        } else {
            when (device.protocolType) {
                ProtocolType.ZIGBEE -> {
                    zigbeeHelper.provideConnectionToDeviceViaMacAddress(device.macAddress)
                }
                ProtocolType.BLUETOOTH -> {
                    bluetoothHelper.provideConnectionToDeviceViaMacAddress(device.macAddress)
                }
                ProtocolType.UNKNOWN -> {}
            }
        }
    }

    fun isDeviceConnected(
        device: Device,
        statusList: List<DeviceConnectionStatus>
    ): Boolean {
        return statusList.find { it.device.macAddress == device.macAddress }?.isConnected
            ?: false
    }

    @Immutable
    data class State(
        val devices: List<Device> = emptyList(),
        val devicesConnectionStatusList: List<DeviceConnectionStatus> = emptyList(),
        val dismissSnackBar: () -> Unit = {},
    )
}
