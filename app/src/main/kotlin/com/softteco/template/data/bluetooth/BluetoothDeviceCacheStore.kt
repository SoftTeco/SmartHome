package com.softteco.template.data.bluetooth

import com.softteco.template.data.base.model.BluetoothDeviceDb
import kotlinx.coroutines.flow.Flow

interface BluetoothDeviceCacheStore {

    suspend fun saveBluetoothDevice(bluetoothDevice: BluetoothDeviceDb): Flow<Unit>

    fun getBluetoothDevices(): Flow<List<BluetoothDeviceDb>>

    fun updateAutoConnectionState(deviceAddress: String, timeStamp: Long)

    fun deleteDevice(deviceAddress: String)

}