package com.softteco.template.data.device.protocol.common

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.softteco.template.data.device.Device
import com.softteco.template.utils.protocol.PermissionType

interface DeviceOperationHandler {
    fun getContext(): Context
    fun getDeviceModel(deviceName: String): Device.Model
    fun getDeviceImage(deviceName: String): String
    fun startIntent(intent: Intent)
    fun registerReceiver(receiver: BroadcastReceiver, filter: IntentFilter)
    fun unregisterReceiver(receiver: BroadcastReceiver)
    fun startConnectionService(serviceClass: Class<out Service>)
    fun stopConnectionService(serviceClass: Class<out Service>)
    fun checkBluetoothSupport(): Boolean
    fun checkEnableDeviceModules(): PermissionType
    fun hasPermissions(): Boolean
}
