package com.softteco.template.data.bluetooth

import android.bluetooth.BluetoothDevice
import com.softteco.template.data.device.protocol.common.BluetoothStateChecker
import com.softteco.template.data.device.protocol.common.DeviceOperationHandler
import com.softteco.template.data.device.protocol.common.IntentLauncher
import com.softteco.template.data.device.protocol.common.PermissionHandler
import com.softteco.template.data.device.protocol.common.ReceiverManager
import com.softteco.template.utils.protocol.DeviceConnectionStatus
import kotlinx.coroutines.flow.StateFlow
import no.nordicsemi.android.support.v18.scanner.ScanResult

interface BluetoothHelper {
    /**
     * Initialize the helper with necessary handlers.
     * This separates concerns and removes direct Activity dependency.
     * 
     * @param deviceOperationHandler Handler for device-specific operations
     * @param permissionHandler Handler for checking and requesting permissions
     * @param intentLauncher Handler for launching system intents
     * @param receiverManager Handler for managing broadcast receivers
     * @param stateChecker Handler for checking Bluetooth and location states
     */
    fun init(
        deviceOperationHandler: DeviceOperationHandler,
        permissionHandler: PermissionHandler,
        intentLauncher: IntentLauncher,
        receiverManager: ReceiverManager,
        stateChecker: BluetoothStateChecker
    )

    /**
     * Clean up resources and clear the helper when the activity is destroyed.
     */
    fun clearResources()

    /**
     * Connect to the Bluetooth device.
     */
    fun connectDevice(bluetoothDevice: BluetoothDevice)

    /**
     * Connect to the Bluetooth device via mac address.
     */
    fun connect(macAddress: String)

    /**
     * Disconnect from the Bluetooth device.
     */
    fun disconnect(macAddress: String)

    /**
     * Register the Bluetooth receiver to track adapter state changes.
     */
    fun registerBluetoothReceiver()

    /**
     * Unregister the Bluetooth receiver.
     */
    fun unregisterBluetoothReceiver()

    /**
     * Checking the device connection status.
     */
    fun checkConnectedDevice(macAddress: String): Boolean

    /**
     * Checking for the presence of a Bluetooth adapter, that it is turned on, and that the
     * necessary permissions have been given.
     */
    fun startScan()

    /**
     * Set callback to receive scan results for discoverable Bluetooth devices.
     */
    fun onScanResult(callback: (scanResult: ScanResult) -> Unit)

    /**
     * Set callback when device is connected.
     */
    fun onDeviceConnected(callback: () -> Unit)

    /**
     * Set callback when device is disconnected.
     */
    fun onDeviceDisconnected(callback: () -> Unit)

    /**
     * Set callback to receive data from the Bluetooth device.
     */
    fun onDeviceDataReceived(callback: () -> Unit)

    /**
     * Set callback for Bluetooth adapter state changes.
     */
    fun onBluetoothStateChanged(callback: (isEnabled: Boolean) -> Unit)

    /**
     * Observe connection statuses of known Bluetooth devices.
     */
    fun observeDeviceConnectionStatus(): StateFlow<Map<String, DeviceConnectionStatus>>
}
