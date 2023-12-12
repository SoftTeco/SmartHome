package com.softteco.template.data.bluetooth.entity

sealed class BluetoothDeviceData {
    data class DataLYWSD03MMC(
        val temperature: Double = 0.0,
        val humidity: Int = 0,
        val battery: Double = 0.0
    ) : BluetoothDeviceData()

    data object Default : BluetoothDeviceData()
}
