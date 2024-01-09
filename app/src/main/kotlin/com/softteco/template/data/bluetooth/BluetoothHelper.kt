package com.softteco.template.data.bluetooth

import android.bluetooth.BluetoothDevice
import com.softteco.template.MainActivity
import com.softteco.template.data.bluetooth.entity.BluetoothDeviceData
import no.nordicsemi.android.support.v18.scanner.ScanResult

interface BluetoothHelper {

    fun init(activity: MainActivity)//Passing the helper class of an Activity instance for the
    // necessary actions when creating an Activity

    fun drop()//Removing an Activity instance from the helper class when the activity is destroyed

    fun connectToDevice(bluetoothDevice: BluetoothDevice)

    fun disconnectFromDevice()

    fun registerReceiver() //Register the receiver to track
    // the events of turning the Bluetooth adapter on and off in relation to
    // displaying the Bluetooth fragment.

    fun unregisterReceiver()//Unregister the receiver to track
    // the events of turning the Bluetooth adapter on and off in relation to
    // displaying the Bluetooth fragment.

    fun operation()//Performing an operation using a Bluetooth adapter

    fun onScanCallback(onScanResult: (scanResult: ScanResult) -> Unit)

    fun onConnectCallback(onConnect: () -> Unit)

    fun onDisconnectCallback(onDisconnect: () -> Unit)

    fun onDeviceResultCallback(onDeviceResult: (bluetoothDeviceData: BluetoothDeviceData) -> Unit)
}
