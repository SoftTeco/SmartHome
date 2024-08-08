package com.softteco.template.data.device

import java.time.LocalDateTime

sealed class ThermometerValues {
    data class DataLYWSD03MMC(
        val temperature: Double = 0.0,
        val humidity: Int = 0,
        val battery: Double = 0.0,
        val macAddress: String = "00:00:00:00:00",
        val timestamp: LocalDateTime = LocalDateTime.now()
    ) : ThermometerValues()

    data object Default : ThermometerValues()
}
