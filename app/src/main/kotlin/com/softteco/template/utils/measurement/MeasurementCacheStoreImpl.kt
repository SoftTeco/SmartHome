package com.softteco.template.utils.measurement

import com.softteco.template.data.base.dao.MeasurementDao
import com.softteco.template.data.base.model.MeasurementDb
import com.softteco.template.data.measurement.MeasurementCacheStore
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MeasurementCacheStoreImpl @Inject constructor(private val measurementDao: MeasurementDao) :
    MeasurementCacheStore {
    override fun saveMeasurement(measurement: MeasurementDb): Long {
        return measurementDao.insertOrUpdate(measurement)
    }

    override fun getMeasurements(): Flow<List<MeasurementDb>> {
        return measurementDao.getList()
    }

    override fun deleteMeasurement(guid: String): Int {
        return measurementDao.delete(guid)
    }
}
