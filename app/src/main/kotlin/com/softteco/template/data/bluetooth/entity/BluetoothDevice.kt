package com.softteco.template.data.bluetooth.entity

data class BluetoothDevice(
    val name: String,
    val macAddress: String,
    val rssi: Int = 0,
    val deviceType: BluetoothDeviceType,
    var connectedLastTime: Long
)
