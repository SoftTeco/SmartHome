package com.softteco.template.data.bluetooth.usecase

import com.softteco.template.data.bluetooth.BluetoothDeviceCacheStore
import javax.inject.Inject

class BluetoothDeviceGetUseCase @Inject constructor(
    private val cacheStore: BluetoothDeviceCacheStore
) {
    fun execute() = cacheStore.getBluetoothDevices()
}
