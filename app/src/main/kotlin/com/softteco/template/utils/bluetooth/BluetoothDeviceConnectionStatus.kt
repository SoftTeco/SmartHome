package com.softteco.template.utils.bluetooth

import com.softteco.template.data.device.Device

data class BluetoothDeviceConnectionStatus(
    val bluetoothDevice: Device,
    var isConnected: Boolean
)
