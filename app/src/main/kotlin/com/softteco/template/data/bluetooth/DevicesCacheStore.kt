package com.softteco.template.data.bluetooth

import com.softteco.template.data.base.model.DeviceDb
import kotlinx.coroutines.flow.Flow

interface DevicesCacheStore {

    fun saveDevice(device: DeviceDb): Long

    fun getDevice(macAddress: String): DeviceDb

    fun getDevices(): Flow<List<DeviceDb>>

    fun deleteDevice(macAddress: String): Int
}
