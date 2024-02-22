package com.softteco.template.utils.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import com.softteco.template.Constants.BIT_SHIFT_VALUE

@OptIn(ExperimentalUnsignedTypes::class)
fun characteristicByteConversation(bytes: ByteArray, startIndex: Int, endIndex: Int): Double {
    val array = bytes.copyOfRange(startIndex, endIndex).toUByteArray()
    var result = 0
    for (i in array.indices) {
        result = result or (array[i].toInt() shl BIT_SHIFT_VALUE * i)
    }
    return result.toDouble()
}

@SuppressLint("MissingPermission")
fun getBluetoothDeviceName(bluetoothDevice: BluetoothDevice): String = bluetoothDevice.name
