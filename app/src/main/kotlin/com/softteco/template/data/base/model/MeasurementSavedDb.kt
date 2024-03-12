package com.softteco.template.data.base.model

import com.softteco.template.data.measurement.entity.MeasurementDevice

abstract class MeasurementSavedDb {
    abstract fun toEntity(): MeasurementDevice
}
