package com.softteco.template.data.bluetooth.usecase

import com.softteco.template.data.bluetooth.BluetoothDeviceCacheStore
import javax.inject.Inject

class BluetoothDeviceDeleteUseCase @Inject constructor(
    private val cacheStore: BluetoothDeviceCacheStore
) {
    fun execute(macAddress: String) = cacheStore.deleteDevice(macAddress)
}
