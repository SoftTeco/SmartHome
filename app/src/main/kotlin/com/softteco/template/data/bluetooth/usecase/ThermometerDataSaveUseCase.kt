package com.softteco.template.data.bluetooth.usecase

import com.softteco.template.data.bluetooth.DevicesDataCacheStore
import com.softteco.template.data.device.model.ThermometerDataDb
import javax.inject.Inject

class ThermometerDataSaveUseCase @Inject constructor(
    private val cacheStore: DevicesDataCacheStore
) {
    fun execute(thermometerDataDb: ThermometerDataDb) = cacheStore.saveResource(thermometerDataDb)
}
