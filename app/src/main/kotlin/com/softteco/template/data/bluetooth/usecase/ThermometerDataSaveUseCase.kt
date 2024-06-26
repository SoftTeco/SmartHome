package com.softteco.template.data.bluetooth.usecase

import com.softteco.template.data.base.model.ThermometerDataDb
import com.softteco.template.data.bluetooth.DevicesDataCacheStore
import javax.inject.Inject

class ThermometerDataSaveUseCase @Inject constructor(
    private val cacheStore: DevicesDataCacheStore
) {
    fun execute(thermometerDataDb: ThermometerDataDb) = cacheStore.saveResource(thermometerDataDb)
}