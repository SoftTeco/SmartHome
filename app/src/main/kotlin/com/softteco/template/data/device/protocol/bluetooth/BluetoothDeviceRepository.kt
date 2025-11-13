package com.softteco.template.data.device.protocol.bluetooth

import com.softteco.template.data.base.error.Result
import com.softteco.template.data.device.Device
import com.softteco.template.data.device.ProtocolType
import com.softteco.template.data.device.ThermometerData
import com.softteco.template.data.device.ThermometerRepository
import com.softteco.template.utils.protocol.DeviceConnectionStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Repository for managing Bluetooth device data and cache.
 */
internal class BluetoothDeviceRepository(
    private val thermometerRepository: ThermometerRepository,
    private val scope: CoroutineScope
) {
    private val savedDevicesCache = mutableSetOf<String>()

    private val _deviceConnectionStatusList = MutableStateFlow<Map<String, DeviceConnectionStatus>>(emptyMap())
    val deviceConnectionStatusList: StateFlow<Map<String, DeviceConnectionStatus>> =
        _deviceConnectionStatusList.asStateFlow()

    fun loadSavedDevices() {
        scope.launch(Dispatchers.IO) {
            thermometerRepository.getDevices()
                .takeIf { it is Result.Success }
                ?.let { it as Result.Success }
                ?.data
                ?.filter { it.protocolType == ProtocolType.BLUETOOTH }
                ?.let { devices ->
                    savedDevicesCache.addAll(devices.map { it.macAddress })
                    devices.forEach { device ->
                        updateDeviceStatus(device.macAddress, DeviceConnectionStatus.disconnected(device))
                    }
                }
        }
    }

    fun updateDeviceStatus(macAddress: String, status: DeviceConnectionStatus) {
        _deviceConnectionStatusList.update { currentMap ->
            currentMap + (macAddress to status)
        }
    }

    fun getDeviceStatus(macAddress: String): DeviceConnectionStatus? =
        _deviceConnectionStatusList.value[macAddress]

    fun isDeviceSaved(macAddress: String): Boolean =
        macAddress in savedDevicesCache

    fun saveNewDevice(device: Device) {
        if (isDeviceSaved(device.macAddress)) return
        
        scope.launch(Dispatchers.IO) {
            thermometerRepository.saveDevice(device)
            thermometerRepository.saveThermometerData(
                ThermometerData(
                    deviceId = device.id,
                    deviceName = device.name,
                    macAddress = device.macAddress
                )
            )
            savedDevicesCache.add(device.macAddress)
        }
    }

    fun isConnected(macAddress: String): Boolean =
        _deviceConnectionStatusList.value[macAddress]?.isConnected ?: false
}
