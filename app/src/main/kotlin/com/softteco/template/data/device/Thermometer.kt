package com.softteco.template.data.device

import java.time.LocalDateTime

data class Thermometer(
    val deviceId: String,
    val deviceName: String,
    val currentTemperature: Float = 0.0f,
    val currentHumidity: Float = 0.0f,
    val temperatureHistory: Map<LocalDateTime, Float> = emptyMap(),
    val humidityHistory: Map<LocalDateTime, Float> = emptyMap(),
)
