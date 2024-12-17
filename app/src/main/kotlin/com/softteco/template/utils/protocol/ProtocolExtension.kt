package com.softteco.template.utils.protocol

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.Service
import android.bluetooth.BluetoothDevice
import android.content.Context
import com.softteco.template.Constants.BIT_SHIFT_VALUE
import com.softteco.template.MainActivity
import com.softteco.template.R
import com.softteco.template.data.device.Device
import com.softteco.template.data.device.ProtocolType
import com.softteco.template.utils.ZigbeeDevice
import kotlinx.coroutines.flow.StateFlow

private const val LYWSD03MMC_IMAGE =
    "file:///android_asset/icon/temperature_monitor_LYWSD03MMC.webp"
private const val ZIGBEE_IMAGE = "file:///android_asset/icon/zigbee.webp"
private const val BLUETOOTH_IMAGE = "file:///android_asset/icon/bluetooth.webp"
private const val UNKNOWN_IMAGE = "file:///android_asset/icon/unknown.webp"

@OptIn(ExperimentalUnsignedTypes::class)
fun bluetoothCharacteristicByteConversation(
    bytes: ByteArray,
    startIndex: Int,
    endIndex: Int
): Double {
    val array = bytes.copyOfRange(startIndex, endIndex).toUByteArray()
    var result = 0
    for (i in array.indices) {
        result = result or (array[i].toInt() shl BIT_SHIFT_VALUE * i)
    }
    return result.toDouble()
}

@SuppressLint("MissingPermission")
fun getBluetoothDeviceName(bluetoothDevice: BluetoothDevice): String = bluetoothDevice.name

fun getZigBeeDeviceName(zigbeeDevice: ZigbeeDevice): String = zigbeeDevice.modelId ?: ""

fun Context.getDeviceModel(deviceName: String): Device.Model = when (deviceName) {
    getString(R.string.temperature_and_humidity_LYWSD03MMC) -> Device.Model.LYWSD03MMC
    else -> Device.Model.Unknown
}

fun Context.getDeviceImage(deviceName: String): String = when (deviceName) {
    getString(R.string.temperature_and_humidity_LYWSD03MMC) -> LYWSD03MMC_IMAGE
    else -> UNKNOWN_IMAGE
}

fun getProtocolImage(protocolType: ProtocolType): String = when (protocolType) {
    ProtocolType.ZIGBEE -> ZIGBEE_IMAGE
    ProtocolType.BLUETOOTH -> BLUETOOTH_IMAGE
    ProtocolType.UNKNOWN -> UNKNOWN_IMAGE
}

fun isServiceRunning(activity: MainActivity?, serviceClass: Class<out Service>): Boolean {
    val manager = activity?.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
    val services = manager?.getRunningServices(Int.MAX_VALUE) ?: return false
    return services.any { it.service.className == serviceClass.name }
}

fun checkRemainingConnectionForService(
    bluetoothDevicesConnectionStatusList: StateFlow<Map<String, DeviceConnectionStatus>>?,
    zigbeeDevicesConnectionStatusList: StateFlow<Map<String, DeviceConnectionStatus>>?
) = (
    bluetoothDevicesConnectionStatusList?.value?.values?.any { it.isConnected } == true ||
        zigbeeDevicesConnectionStatusList?.value?.values?.any { it.isConnected } == true
    )
