package com.softteco.template.data.bluetooth

import com.softteco.template.data.device.Device

interface BluetoothByteParser {

    fun parseBytes(bytes: ByteArray, bluetoothDeviceModel: Device.Model): Any
}
