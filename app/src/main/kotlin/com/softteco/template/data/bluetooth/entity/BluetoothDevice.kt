package com.softteco.template.data.bluetooth.entity

data class BluetoothDevice(
    val name: String,
    val macAddress: String,
    val rssi: Int = 0,
    val bluetoothDeviceType: BluetoothDeviceType,
    var connectedLastTime: Long
)
