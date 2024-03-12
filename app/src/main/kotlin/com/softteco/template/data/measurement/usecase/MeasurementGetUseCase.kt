package com.softteco.template.data.measurement.usecase

import com.softteco.template.data.measurement.MeasurementCacheStore
import javax.inject.Inject

class MeasurementGetUseCase @Inject constructor(
    private val cacheStore: MeasurementCacheStore
) {
    fun execute(macAddressOfDevice: String) = cacheStore.getMeasurements(macAddressOfDevice)
}
