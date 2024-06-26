package com.softteco.template.data.bluetooth.usecase

import com.softteco.template.data.bluetooth.DevicesDataCacheStore
import javax.inject.Inject

class ThermometerDataGetUseCase @Inject constructor(
    private val cacheStore: DevicesDataCacheStore
) {
    fun execute(macAddress: String) = cacheStore.getResource(macAddress)
}