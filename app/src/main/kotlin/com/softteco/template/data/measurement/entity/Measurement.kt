package com.softteco.template.data.measurement.entity

sealed class Measurement {
    data class MeasurementLYWSD03MMC(
        val guid: String = "",
        val temperature: Double = 0.0,
        val humidity: Int = 0,
        val battery: Double = 0.0,
        val macAddressOfDevice: String = ""
    ) : Measurement()

    data object Default : Measurement()
}
