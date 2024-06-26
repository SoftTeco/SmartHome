package com.softteco.template.utils.bluetooth

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import androidx.annotation.RequiresApi
import com.softteco.template.MainActivity
import com.softteco.template.data.bluetooth.BluetoothPermissionChecker
import javax.inject.Inject
import javax.inject.Singleton

const val REQUEST_ALLOW_BT = 1

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

@Singleton
internal class BluetoothPermissionCheckerImpl @Inject constructor() : BluetoothPermissionChecker {
    override fun checkBluetoothSupport(
        bluetoothAdapter: BluetoothAdapter?,
        activity: MainActivity
    ) = bluetoothAdapter != null &&
            activity.packageManager?.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE) == true

    override fun checkEnableDeviceModules(
        bluetoothAdapter: BluetoothAdapter?,
        locationManager: LocationManager?
    ): PermissionType {
        var permissionType = PermissionType.BLUETOOTH_AND_LOCATION_TURNED_ON
        locationManager?.let {
            if (!it.isProviderEnabled(LocationManager.GPS_PROVIDER) && !it.isProviderEnabled(
                    LocationManager.NETWORK_PROVIDER
                )
            ) {
                permissionType = PermissionType.LOCATION_TURNED_OFF
            }
        }
        bluetoothAdapter?.let { if (!it.isEnabled) return PermissionType.BLUETOOTH_TURNED_OFF }
        return permissionType
    }

    override fun hasPermissions(activity: MainActivity): Boolean {
        val permissionsToCheck = when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> PERMISSIONS_FOR_BLUETOOTH_AFTER_ANDROID_12
            else -> PERMISSIONS_FOR_BLUETOOTH_BEFORE_ANDROID_12
        }

        return permissionsToCheck.all {
            requestPermission(activity, it, permissionsToCheck)
        }
    }

    private fun requestPermission(
        activity: MainActivity,
        permissionValue: String,
        permissions: Array<String>
    ): Boolean {
        return when {
            activity.checkSelfPermission(permissionValue) != PackageManager.PERMISSION_GRANTED -> {
                activity.requestPermissions(
                    permissions,
                    REQUEST_ALLOW_BT
                )
                false
            }

            activity.checkSelfPermission(permissionValue) == PackageManager.PERMISSION_DENIED -> false
            else -> true
        }
    }
}
