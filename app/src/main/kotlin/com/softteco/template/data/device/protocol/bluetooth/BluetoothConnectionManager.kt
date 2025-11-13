package com.softteco.template.data.device.protocol.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.content.Context
import com.softteco.template.utils.protocol.getBluetoothAdapter
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Manages Bluetooth device connections and GATT operations.
 */
@SuppressLint("MissingPermission")
internal class BluetoothConnectionManager(
    @ApplicationContext private val context: Context,
    private val scope: CoroutineScope,
    private val gattCallback: BluetoothGattCallbackHandler
) {
    private val connectedDevices = hashMapOf<String, BluetoothGatt>()

    fun connect(macAddress: String, onConnecting: () -> Unit = {}) {
        context.getBluetoothAdapter()
            ?.getRemoteDevice(macAddress)
            ?.let { device ->
                onConnecting()
                scope.launch(Dispatchers.IO) {
                    connectDevice(device)
                }
            } ?: Timber.w("Bluetooth adapter not available for connection")
    }

    fun connectDevice(bluetoothDevice: BluetoothDevice) {
        bluetoothDevice.connectGatt(
            context,
            false,
            gattCallback,
            BluetoothDevice.TRANSPORT_LE
        )
    }

    fun disconnect(macAddress: String) {
        connectedDevices[macAddress]?.disconnect()
    }

    fun isConnected(macAddress: String): Boolean = connectedDevices.containsKey(macAddress)

    fun addConnectedDevice(macAddress: String, gatt: BluetoothGatt) {
        connectedDevices[macAddress] = gatt
    }

    fun removeConnectedDevice(macAddress: String) {
        connectedDevices.remove(macAddress)
    }

    fun closeConnection(gatt: BluetoothGatt) {
        gatt.close()
        removeConnectedDevice(gatt.device.address)
    }
}
