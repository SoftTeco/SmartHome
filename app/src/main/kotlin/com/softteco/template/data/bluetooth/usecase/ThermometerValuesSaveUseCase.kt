package com.softteco.template.data.bluetooth.usecase

import com.softteco.template.data.base.model.ThermometerValuesDb
import com.softteco.template.data.bluetooth.DevicesDataCacheStore
import javax.inject.Inject

class ThermometerValuesSaveUseCase @Inject constructor(
    private val cacheStore: DevicesDataCacheStore
) {
    fun execute(thermometerValuesDb: ThermometerValuesDb) =
        cacheStore.saveThermometerValues(thermometerValuesDb)
}