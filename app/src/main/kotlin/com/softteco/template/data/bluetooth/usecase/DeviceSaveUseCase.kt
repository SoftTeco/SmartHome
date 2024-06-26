package com.softteco.template.data.bluetooth.usecase

import com.softteco.template.data.base.model.DeviceDb
import com.softteco.template.data.bluetooth.DevicesCacheStore
import javax.inject.Inject

class DeviceSaveUseCase @Inject constructor(
    private val cacheStore: DevicesCacheStore
) {
    fun execute(deviceDb: DeviceDb) = cacheStore.saveDevice(deviceDb)
}
