package com.softteco.template.data.bluetooth

import com.softteco.template.data.base.model.BluetoothDeviceDb
import kotlinx.coroutines.flow.Flow

interface BluetoothDeviceCacheStore {
    fun saveBluetoothDevice(bluetoothDevice: BluetoothDeviceDb): Long

    fun getBluetoothDevices(): Flow<List<BluetoothDeviceDb>>

    fun updateLastConnectionTimeStamp(macAddress: String, connectedLastTime: Long): Int

    fun deleteDevice(macAddress: String): Int
}
