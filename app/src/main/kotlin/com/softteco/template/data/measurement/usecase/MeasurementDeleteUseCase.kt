package com.softteco.template.data.measurement.usecase

import com.softteco.template.data.measurement.MeasurementCacheStore
import javax.inject.Inject

class MeasurementDeleteUseCase @Inject constructor(
    private val cacheStore: MeasurementCacheStore
) {
    fun execute(guid: String) = cacheStore.deleteMeasurement(guid)
}
