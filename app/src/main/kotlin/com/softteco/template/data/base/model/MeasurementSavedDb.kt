package com.softteco.template.data.base.model

import com.softteco.template.data.measurement.entity.Measurement

abstract class MeasurementSavedDb {
    abstract fun toEntity(): Measurement.MeasurementLYWSD03MMC
}
