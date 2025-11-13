package com.softteco.template.data.device.protocol.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothProfile
import com.softteco.template.BuildConfig
import com.softteco.template.Constants.READ_BLUETOOTH_CHARACTERISTIC_DELAY
import com.softteco.template.data.bluetooth.BluetoothByteParser
import com.softteco.template.data.device.Device
import com.softteco.template.data.device.ThermometerRepository
import com.softteco.template.data.device.ThermometerValues
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalDateTime
import java.util.UUID

/**
 * Handles Bluetooth GATT callbacks for device connection and data reception.
 */
@SuppressLint("MissingPermission")
internal class BluetoothGattCallbackHandler(
    private val bluetoothByteParser: BluetoothByteParser,
    private val thermometerRepository: ThermometerRepository,
    private val deviceRepository: BluetoothDeviceRepository,
    private val scope: CoroutineScope,
    private val onDeviceConnected: (BluetoothGatt) -> Unit,
    private val onDeviceDisconnected: (BluetoothGatt) -> Unit,
    private val onDeviceDataReceived: () -> Unit
) : BluetoothGattCallback() {

    private companion object {
        val NOTIFICATION_DISABLE_VALUE = byteArrayOf(0x00, 0x00)
    }

    private var readCharacteristicTimestamp = 0L

    override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
        when (newState) {
            BluetoothProfile.STATE_CONNECTED -> onDeviceConnected(gatt)
            BluetoothProfile.STATE_DISCONNECTED -> onDeviceDisconnected(gatt)
        }
    }

    override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
        super.onServicesDiscovered(gatt, status)
        
        when (status) {
            BluetoothGatt.GATT_SUCCESS -> setupCharacteristicNotification(gatt)
            else -> Timber.e("Service discovery failed with status: $status")
        }
    }

    override fun onCharacteristicChanged(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
        value: ByteArray
    ) {
        handleCharacteristicChange(gatt, value)
    }
    
    @Deprecated("Deprecated in Java")
    override fun onCharacteristicChanged(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic
    ) {
        @Suppress("DEPRECATION")
        handleCharacteristicChange(gatt, characteristic.value)
    }

    fun resetTimestamp() {
        readCharacteristicTimestamp = 0L
    }

    private fun setupCharacteristicNotification(gatt: BluetoothGatt) {
        val service = gatt.getService(UUID.fromString(BuildConfig.BLUETOOTH_SERVICE_UUID_VALUE)) ?: run {
            Timber.e("Service not found")
            return
        }
        
        val characteristic = service.getCharacteristic(
            UUID.fromString(BuildConfig.BLUETOOTH_CHARACTERISTIC_UUID_VALUE)
        ) ?: run {
            Timber.e("Characteristic not found")
            return
        }
        
        setCharacteristicNotification(gatt, characteristic, enable = true)
    }

    private fun setCharacteristicNotification(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
        enable: Boolean
    ) {
        gatt.setCharacteristicNotification(characteristic, enable)
        
        val descriptor = characteristic.getDescriptor(
            UUID.fromString(BuildConfig.BLUETOOTH_DESCRIPTOR_UUID_VALUE)
        ) ?: run {
            Timber.e("Descriptor not found")
            return
        }
        
        val descriptorValue = when {
            enable -> BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
            else -> NOTIFICATION_DISABLE_VALUE
        }
        
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            gatt.writeDescriptor(descriptor, descriptorValue)
        } else {
            @Suppress("DEPRECATION")
            descriptor.value = descriptorValue
            @Suppress("DEPRECATION")
            gatt.writeDescriptor(descriptor)
        }
    }

    private fun handleCharacteristicChange(gatt: BluetoothGatt, value: ByteArray) {
        if (!shouldProcessCharacteristic()) return
        
        readCharacteristicTimestamp = System.currentTimeMillis()
        
        val device = deviceRepository.getDeviceStatus(gatt.device.address)?.device ?: run {
            Timber.w("Device not found for address: ${gatt.device.address}")
            return
        }
        
        parseAndSaveThermometerData(value, device)
        onDeviceDataReceived()
    }

    private fun shouldProcessCharacteristic(): Boolean =
        System.currentTimeMillis() - readCharacteristicTimestamp >= READ_BLUETOOTH_CHARACTERISTIC_DELAY

    private fun parseAndSaveThermometerData(value: ByteArray, device: Device) {
        val data = bluetoothByteParser.parseBytes(value, device.model) as? ThermometerValues.DataLYWSD03MMC
            ?: return
        
        scope.launch(Dispatchers.IO) {
            thermometerRepository.saveCurrentMeasurement(
                ThermometerValues.DataLYWSD03MMC(
                    temperature = data.temperature,
                    humidity = data.humidity,
                    battery = data.battery,
                    macAddress = device.macAddress,
                    timestamp = LocalDateTime.now()
                )
            )
        }
    }
}

