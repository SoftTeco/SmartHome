package com.softteco.template.data.measurement.usecase

import com.softteco.template.data.base.model.MeasurementDb
import com.softteco.template.data.measurement.MeasurementCacheStore
import javax.inject.Inject

class MeasurementSaveUseCase @Inject constructor(
    private val cacheStore: MeasurementCacheStore
) {
    fun execute(measurementDb: MeasurementDb) = cacheStore.saveMeasurement(measurementDb)
}
