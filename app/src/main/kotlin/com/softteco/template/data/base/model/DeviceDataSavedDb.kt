package com.softteco.template.data.base.model

import com.softteco.template.data.device.Device

abstract class DeviceDataSavedDb {
    abstract fun toEntity(): Device.Basic
}
