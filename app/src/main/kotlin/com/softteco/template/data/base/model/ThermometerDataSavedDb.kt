package com.softteco.template.data.base.model

import com.softteco.template.data.device.ThermometerData

abstract class ThermometerDataSavedDb {
    abstract fun toEntity(): ThermometerData
}
