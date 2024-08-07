package com.softteco.template.data.device.model

import com.softteco.template.data.device.ThermometerData

abstract class ThermometerDataSavedDb {
    abstract fun toEntity(): ThermometerData
}
