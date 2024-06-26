package com.softteco.template.utils.bluetooth

import com.softteco.template.data.base.dao.DevicesDao
import com.softteco.template.data.base.model.DeviceDb
import com.softteco.template.data.bluetooth.DevicesCacheStore
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DevicesCacheStoreImpl @Inject constructor(private val devicesDao: DevicesDao) :
    DevicesCacheStore {

    override fun saveDevice(device: DeviceDb): Long {
        return devicesDao.insertOrUpdate(device)
    }

    override fun getDevice(macAddress: String): DeviceDb {
        return devicesDao.getDevice(macAddress)
    }

    override fun getDevices(): Flow<List<DeviceDb>> {
        return devicesDao.getListDevices()
    }

    override fun deleteDevice(macAddress: String): Int {
        return devicesDao.delete(macAddress)
    }
}
