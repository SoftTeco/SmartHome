package com.softteco.template.data.bluetooth

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import com.softteco.template.MainActivity
import com.softteco.template.utils.bluetooth.BluetoothDeviceConnectionStatus
import no.nordicsemi.android.support.v18.scanner.ScanResult

interface BluetoothHelper {
    /**
     * Passing the helper class of an Activity instance for the necessary actions when
     * creating an Activity.
     */
    fun init(activity: MainActivity)

    /**
     * Removing an Activity instance from the helper class when the activity is destroyed.
     */
    fun drop()

    /**
     * Connecting to the Bluetooth device.
     */
    fun provideConnectionToDevice(bluetoothDevice: BluetoothDevice)

    /**
     * Disconnecting from the Bluetooth device.
     */
    fun disconnectFromDevice(bluetoothGatt: BluetoothGatt?)

    /**
     * Register the receiver to track the events of turning the Bluetooth adapter on and off in
     * relation to displaying the Bluetooth fragment.
     */
    fun registerReceiver()

    /**
     * Unregister the receiver to track the events of turning the Bluetooth adapter on and off in
     * relation to displaying the Bluetooth fragment.
     */
    fun unregisterReceiver()

    /**
     * Checking for the presence of a Bluetooth adapter, that it is turned on, and that the
     * necessary permissions have been given.
     */
    fun startScanIfHasPermissions()

    /**
     * Callback to receive scan results for discoverable Bluetooth devices.
     */
    fun onScanCallback(onScanResult: (scanResult: ScanResult) -> Unit)

    /**
     * Callback to connect to the Bluetooth device.
     */
    fun onConnectCallback(onConnect: () -> Unit)

    /**
     * Callback to disconnect from the Bluetooth device.
     */
    fun onDisconnectCallback(onDisconnect: () -> Unit)

    /**
     * Callback to receive data from the Bluetooth device.
     */
    fun onDeviceResultCallback(onDeviceResult: () -> Unit)

    /**
     * Callback to catch action with current state of Bluetooth module.
     */
    fun onBluetoothModuleChangeStateCallback(onBluetoothModuleChangeState: (ifTurnOn: Boolean) -> Unit)

    /**
     * Get connection statuses of known Bluetooth devices.
     */
    fun getDeviceConnectionStatusList(): HashMap<String, BluetoothDeviceConnectionStatus>

    /**
     * Set currently viewed Bluetooth device address for Charts Screen.
     */
    fun setCurrentlyViewedBluetoothDeviceAddress(macAddress: String)

    /**
     * Get currently viewed Bluetooth device address for Charts Screen.
     */
    fun getCurrentlyViewedBluetoothDeviceAddress(): String
}
