package com.softteco.template.data.device

import java.time.LocalDateTime
import java.util.UUID

data class ThermometerData(
    val deviceId: UUID = UUID.randomUUID(),
    val deviceName: String = "",
    val macAddress: String = "",
    var currentTemperature: Double = 0.0,
    var currentHumidity: Int = 0,
    val valuesHistory: List<ThermometerValues> = listOf(),
    var temperatureHistory:  Map<LocalDateTime, Float> = emptyMap(),
    var humidityHistory:  Map<LocalDateTime, Float> = emptyMap()
)
