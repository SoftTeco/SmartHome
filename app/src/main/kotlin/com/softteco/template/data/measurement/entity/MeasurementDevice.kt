package com.softteco.template.data.measurement.entity

import com.softteco.template.data.bluetooth.entity.BluetoothDeviceType

data class MeasurementDevice(
    val guid: String = "",
    val temperature: Double = 0.0,
    val humidity: Int = 0,
    val battery: Double = 0.0,
    val bluetoothDeviceType: BluetoothDeviceType,
    val macAddressOfDevice: String = ""
)
