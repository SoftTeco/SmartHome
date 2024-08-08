package com.softteco.template.data.device.model

import com.softteco.template.data.device.ThermometerValues

abstract class ThermometerValuesSavedDb {
    abstract fun toEntity(): ThermometerValues
}
