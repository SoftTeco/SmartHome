package com.softteco.template.utils.bluetooth

import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothDevice
import android.content.Context
import com.softteco.template.Constants.BIT_SHIFT_VALUE
import com.softteco.template.R
import com.softteco.template.data.device.Device

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

fun Context.getBluetoothDeviceModel(deviceName: String): Device.Model = when(deviceName) {
    getString(R.string.temperature_and_humidity_LYWSD03MMC) -> Device.Model.LYWSD03MMC
    else -> Device.Model.Unknown
}
