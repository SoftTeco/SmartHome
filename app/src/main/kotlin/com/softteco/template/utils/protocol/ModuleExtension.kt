package com.softteco.template.utils.protocol

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import androidx.annotation.RequiresApi

const val REQUEST_ALLOW_BT = 1
const val ACTION_CONNECTION_SERVICE = "CONNECTION_SERVICE"

val PERMISSIONS_FOR_BLUETOOTH_BEFORE_ANDROID_12 = arrayOf(
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.ACCESS_COARSE_LOCATION
)

@RequiresApi(Build.VERSION_CODES.S)
val PERMISSIONS_FOR_BLUETOOTH_AFTER_ANDROID_12 = arrayOf(
    Manifest.permission.BLUETOOTH_SCAN,
    Manifest.permission.BLUETOOTH_CONNECT,
    Manifest.permission.BLUETOOTH_ADVERTISE
)

enum class PermissionType {
    BLUETOOTH_TURNED_OFF, LOCATION_TURNED_OFF, BLUETOOTH_AND_LOCATION_TURNED_ON
}

fun Context.getBluetoothManager(): BluetoothManager? =
    getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager?

fun Context.getBluetoothAdapter(): BluetoothAdapter? = getBluetoothManager()?.adapter

fun Context.getLocationManager(): LocationManager? =
    getSystemService(Context.LOCATION_SERVICE) as LocationManager?

fun Context.checkBluetoothSupport() =
    getBluetoothManager()?.adapter != null &&
        packageManager?.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE) == true

fun Context.checkEnableDeviceModules(): PermissionType {
    var permissionType = PermissionType.BLUETOOTH_AND_LOCATION_TURNED_ON
    getLocationManager()?.let {
        if (!it.isProviderEnabled(LocationManager.GPS_PROVIDER) && !it.isProviderEnabled(
                LocationManager.NETWORK_PROVIDER
            )
        ) {
            permissionType = PermissionType.LOCATION_TURNED_OFF
        }
    }
    getBluetoothManager()?.adapter?.let { if (!it.isEnabled) return PermissionType.BLUETOOTH_TURNED_OFF }
    return permissionType
}

fun Activity.hasPermissions(): Boolean {
    val permissionsToCheck = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> PERMISSIONS_FOR_BLUETOOTH_AFTER_ANDROID_12
        else -> PERMISSIONS_FOR_BLUETOOTH_BEFORE_ANDROID_12
    }

    return permissionsToCheck.all {
        when {
            checkSelfPermission(it) != PackageManager.PERMISSION_GRANTED -> {
                requestPermissions(
                    permissionsToCheck,
                    REQUEST_ALLOW_BT
                )
                false
            }

            checkSelfPermission(it) == PackageManager.PERMISSION_DENIED -> false
            else -> true
        }
    }
}

@SuppressLint("UnspecifiedRegisterReceiverFlag")
fun Activity.registerCustomReceiver(receiver: BroadcastReceiver, filter: IntentFilter) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
    } else {
        registerReceiver(receiver, filter)
    }
}

fun Context.startConnectionService(serviceClass: Class<out Service>) {
    val intent = Intent(this, serviceClass).apply {
        action = ACTION_CONNECTION_SERVICE
    }
    startForegroundService(intent)
}

fun Context.stopConnectionService(serviceClass: Class<out Service>) {
    val intent = Intent(this, serviceClass).apply {
        action = ACTION_CONNECTION_SERVICE
    }
    stopService(intent)
}

fun isServiceRunning(serviceClass: Class<out Service>): Boolean {
    return when (serviceClass) {
        DeviceConnectionService::class.java -> ServiceStateManager.isDeviceConnectionServiceRunning()
        else -> {
            timber.log.Timber.w("Service state tracking not implemented for ${serviceClass.name}")
            false
        }
    }
}
