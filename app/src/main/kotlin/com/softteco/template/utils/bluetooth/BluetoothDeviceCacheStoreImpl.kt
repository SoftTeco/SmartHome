package com.softteco.template.utils.bluetooth

import com.softteco.template.data.base.dao.BluetoothDeviceDao
import com.softteco.template.data.base.model.BluetoothDeviceDb
import com.softteco.template.data.bluetooth.BluetoothDeviceCacheStore
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class BluetoothDevicesCacheStoreImpl @Inject constructor(private val bluetoothDevicesDao: BluetoothDeviceDao) :
    BluetoothDeviceCacheStore {
    override suspend fun saveBluetoothDevice(bluetoothDevice: BluetoothDeviceDb): Flow<Unit> {
        bluetoothDevicesDao.insertOrUpdate(bluetoothDevice)
        return bluetoothDevicesDao.insertBluetoothDeviceAndGetId(bluetoothDevice)
    }

    override fun getBluetoothDevices(): Flow<List<BluetoothDeviceDb>> {
        return bluetoothDevicesDao.getBluetoothDevices()
    }

    override fun updateAutoConnectionState(deviceAddress: String, timeStamp: Long) {
        TODO("Not yet implemented")
    }

    override fun deleteDevice(deviceAddress: String) {
        TODO("Not yet implemented")
    }


}