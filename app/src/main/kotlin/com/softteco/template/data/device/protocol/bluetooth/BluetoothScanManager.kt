package com.softteco.template.data.device.protocol.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.provider.Settings
import com.softteco.template.data.device.Device
import com.softteco.template.data.device.ProtocolType
import com.softteco.template.data.device.protocol.common.BluetoothStateChecker
import com.softteco.template.data.device.protocol.common.DeviceOperationHandler
import com.softteco.template.data.device.protocol.common.IntentLauncher
import com.softteco.template.data.device.protocol.common.PermissionHandler
import com.softteco.template.utils.protocol.DeviceConnectionStatus
import com.softteco.template.utils.protocol.PermissionType
import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat
import no.nordicsemi.android.support.v18.scanner.ScanCallback
import no.nordicsemi.android.support.v18.scanner.ScanResult
import timber.log.Timber
import java.util.UUID

/**
 * Manages Bluetooth LE scanning operations.
 */
@SuppressLint("MissingPermission")
internal class BluetoothScanManager(
    private val deviceOperationHandler: DeviceOperationHandler,
    private val permissionHandler: PermissionHandler,
    private val intentLauncher: IntentLauncher,
    private val stateChecker: BluetoothStateChecker,
    private val onDeviceDiscovered: (String, DeviceConnectionStatus) -> Unit,
    private val onScanResultCallback: ((ScanResult) -> Unit)?
) {
    private companion object {
        const val DEFAULT_DEVICE_NAME = "Temperature and Humidity Monitor"
    }

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, scanResult: ScanResult) {
            super.onScanResult(callbackType, scanResult)
            handleScanResult(scanResult)
        }
    }

    fun startScan() {
        val moduleState = validatePreconditions() ?: return

        when (moduleState) {
            PermissionType.LOCATION_TURNED_OFF -> intentLauncher.launchIntent(
                Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            )
            PermissionType.BLUETOOTH_TURNED_OFF -> intentLauncher.launchIntent(
                Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            )
            PermissionType.BLUETOOTH_AND_LOCATION_TURNED_ON -> performScan()
        }
    }

    fun stopScan() {
        BluetoothLeScannerCompat.getScanner().stopScan(scanCallback)
    }

    private fun validatePreconditions(): PermissionType? {
        return when {
            !stateChecker.isBluetoothSupported() -> {
                Timber.w("Bluetooth is not supported")
                null
            }
            !permissionHandler.hasBluetoothPermissions() -> {
                Timber.w("Bluetooth permissions are not granted")
                null
            }
            else -> stateChecker.checkModulesState()
        }
    }

    private fun performScan() {
        stopScan()
        BluetoothLeScannerCompat.getScanner().startScan(scanCallback)
    }

    private fun handleScanResult(scanResult: ScanResult) {
        val deviceName = scanResult.device.name ?: return
        val device = createDevice(scanResult, deviceName)
        val status = DeviceConnectionStatus.searching(device)

        onDeviceDiscovered(scanResult.device.address, status)
        onScanResultCallback?.invoke(scanResult)
    }

    private fun createDevice(scanResult: ScanResult, deviceName: String): Device.Basic {
        return Device.Basic(
            type = Device.Type.TemperatureAndHumidity,
            family = Device.Family.Sensor,
            model = deviceOperationHandler.getDeviceModel(deviceName),
            id = UUID.randomUUID(),
            defaultName = deviceName,
            name = DEFAULT_DEVICE_NAME,
            macAddress = scanResult.device.address,
            img = deviceOperationHandler.getDeviceImage(deviceName),
            location = "",
            protocolType = ProtocolType.BLUETOOTH
        )
    }
}
