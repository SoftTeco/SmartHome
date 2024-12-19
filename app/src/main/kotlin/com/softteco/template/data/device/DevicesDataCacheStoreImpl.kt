package com.softteco.template.data.device

import com.softteco.template.data.bluetooth.DevicesDataCacheStore
import com.softteco.template.data.device.dao.ThermometerDataDao
import com.softteco.template.data.device.model.ThermometerDataDb
import com.softteco.template.data.device.model.ThermometerValuesDb
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DevicesDataCacheStoreImpl @Inject constructor(private val thermometerDataDao: ThermometerDataDao) :
    DevicesDataCacheStore {

    override fun saveResource(thermometerData: ThermometerDataDb): Long {
        return thermometerDataDao.saveResource(thermometerData)
    }

    override fun getResource(macAddress: String): ThermometerDataDb {
        return thermometerDataDao.getResource(macAddress)
    }

    override fun saveThermometerValues(thermometerValues: ThermometerValuesDb): Long {
        return thermometerDataDao.insertOrUpdate(thermometerValues)
    }

    override fun getThermometerValues(macAddress: String): ThermometerValuesDb {
        return thermometerDataDao.getThermometerValue(macAddress)
    }

    override fun deleteResource(macAddress: String): Int {
        return thermometerDataDao.deleteResource(macAddress)
    }
}
