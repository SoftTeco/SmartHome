package com.softteco.template.data.bluetooth.usecase

import com.softteco.template.data.bluetooth.DevicesDataCacheStore
import javax.inject.Inject

class ThermometerValuesGetUseCase @Inject constructor(
    private val cacheStore: DevicesDataCacheStore
) {
    fun execute(macAddress: String) = cacheStore.getThermometerValues(macAddress)
}
