package com.softteco.template.data.bluetooth.usecase

import com.softteco.template.data.bluetooth.DevicesCacheStore
import com.softteco.template.data.device.model.DeviceDb
import javax.inject.Inject

class DeviceSaveUseCase @Inject constructor(
    private val cacheStore: DevicesCacheStore
) {
    fun execute(deviceDb: DeviceDb) = cacheStore.saveDevice(deviceDb)
}
