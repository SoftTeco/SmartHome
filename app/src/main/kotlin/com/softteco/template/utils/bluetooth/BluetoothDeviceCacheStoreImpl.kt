package com.softteco.template.utils.bluetooth

import com.softteco.template.data.base.dao.BluetoothDeviceDao
import com.softteco.template.data.base.model.BluetoothDeviceDb
import com.softteco.template.data.bluetooth.BluetoothDeviceCacheStore
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BluetoothDeviceCacheStoreImpl @Inject constructor(private val bluetoothDevicesDao: BluetoothDeviceDao) :
    BluetoothDeviceCacheStore {
    override fun saveBluetoothDevice(bluetoothDevice: BluetoothDeviceDb): Long {
        return bluetoothDevicesDao.insertOrUpdate(bluetoothDevice)
    }

    override fun getBluetoothDevices(): Flow<List<BluetoothDeviceDb>> {
        return bluetoothDevicesDao.getList()
    }

    override fun updateLastConnectionTimeStamp(macAddress: String, connectedLastTime: Long): Int {
        return bluetoothDevicesDao.updateLastConnectionTimeStamp(macAddress, connectedLastTime)
    }

    override fun deleteDevice(macAddress: String): Int {
        return bluetoothDevicesDao.delete(macAddress)
    }
}
