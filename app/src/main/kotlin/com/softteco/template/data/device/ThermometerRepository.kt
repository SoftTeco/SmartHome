package com.softteco.template.data.device

import com.softteco.template.data.base.error.Result

interface ThermometerRepository {

    suspend fun getThermometerData(): Result<Thermometer>
    suspend fun getCurrentMeasurement(): Result<Float>
}
