package com.softteco.template.data.bluetooth.usecase

import com.softteco.template.data.base.model.BluetoothDeviceDb
import com.softteco.template.data.bluetooth.BluetoothDeviceCacheStore
import javax.inject.Inject

class BluetoothDeviceSaveUseCase @Inject constructor(
    private val cacheStore: BluetoothDeviceCacheStore
) {
    fun execute(bluetoothDeviceDb: BluetoothDeviceDb) = cacheStore.saveBluetoothDevice(bluetoothDeviceDb)
}
