package com.softteco.template.data.base.model

import com.softteco.template.data.device.ThermometerValues

abstract class ThermometerValuesSavedDb {
    abstract fun toEntity(): ThermometerValues
}
