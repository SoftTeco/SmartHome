package com.softteco.template.data.bluetooth

import com.softteco.template.data.bluetooth.entity.BluetoothDeviceType

interface BluetoothByteParser {

    fun parseBytes(bytes: ByteArray, bluetoothDeviceType: BluetoothDeviceType): Any
}