package com.softteco.template.data.measurement

import com.softteco.template.data.base.model.MeasurementDb
import kotlinx.coroutines.flow.Flow

interface MeasurementCacheStore {
    fun saveMeasurement(measurement: MeasurementDb): Long

    fun getMeasurements(macAddressOfDevice: String): Flow<List<MeasurementDb>>

    fun deleteMeasurement(guid: String): Int
}
