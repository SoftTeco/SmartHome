package com.softteco.template.data.bluetooth.usecase

import com.softteco.template.data.bluetooth.DevicesDataCacheStore
import com.softteco.template.data.device.model.ThermometerValuesDb
import javax.inject.Inject

class ThermometerValuesSaveUseCase @Inject constructor(
    private val cacheStore: DevicesDataCacheStore
) {
    fun execute(thermometerValuesDb: ThermometerValuesDb) =
        cacheStore.saveThermometerValues(thermometerValuesDb)
}
