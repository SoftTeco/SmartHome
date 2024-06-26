package com.softteco.template.data.bluetooth.usecase

import com.softteco.template.data.bluetooth.DevicesCacheStore
import javax.inject.Inject

class DevicesGetUseCase @Inject constructor(
    private val cacheStore: DevicesCacheStore
) {
    fun execute() = cacheStore.getDevices()
}