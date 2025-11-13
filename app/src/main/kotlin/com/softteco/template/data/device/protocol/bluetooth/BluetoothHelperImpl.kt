package com.softteco.template.data.device.protocol.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.softteco.template.data.bluetooth.BluetoothByteParser
import com.softteco.template.data.bluetooth.BluetoothHelper
import com.softteco.template.data.bluetooth.BluetoothState
import com.softteco.template.data.device.ThermometerRepository
import com.softteco.template.data.device.protocol.common.BluetoothStateChecker
import com.softteco.template.data.device.protocol.common.DeviceOperationHandler
import com.softteco.template.data.device.protocol.common.IntentLauncher
import com.softteco.template.data.device.protocol.common.PermissionHandler
import com.softteco.template.data.device.protocol.common.ReceiverManager
import com.softteco.template.utils.protocol.DeviceConnectionService
import com.softteco.template.utils.protocol.DeviceConnectionStatus
import com.softteco.template.utils.protocol.isServiceRunning
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.StateFlow
import no.nordicsemi.android.support.v18.scanner.ScanResult
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Main coordinator for Bluetooth operations.
 * Delegates responsibilities to specialized managers.
 */
@SuppressLint("MissingPermission")
@Singleton
internal class BluetoothHelperImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    bluetoothByteParser: BluetoothByteParser,
    thermometerRepository: ThermometerRepository
) : BluetoothHelper, BluetoothState {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private lateinit var deviceOperationHandler: DeviceOperationHandler
    private lateinit var receiverManager: ReceiverManager
    private lateinit var bluetoothReceiver: BroadcastReceiver

    private lateinit var scanManager: BluetoothScanManager
    private val connectionManager: BluetoothConnectionManager
    private val deviceRepository: BluetoothDeviceRepository
    private val gattHandler: BluetoothGattCallbackHandler

    override var deviceConnectedCallback: (() -> Unit)? = null
    override var deviceDisconnectedCallback: (() -> Unit)? = null
    override var scanResultCallback: ((scanResult: ScanResult) -> Unit)? = null
    override var deviceDataReceivedCallback: (() -> Unit)? = null
    override var bluetoothStateChangedCallback: ((isEnabled: Boolean) -> Unit)? = null

    init {
        deviceRepository = BluetoothDeviceRepository(thermometerRepository, scope)
        
        gattHandler = BluetoothGattCallbackHandler(
            bluetoothByteParser = bluetoothByteParser,
            thermometerRepository = thermometerRepository,
            deviceRepository = deviceRepository,
            scope = scope,
            onDeviceConnected = ::handleDeviceConnected,
            onDeviceDisconnected = ::handleDeviceDisconnected,
            onDeviceDataReceived = { deviceDataReceivedCallback?.invoke() }
        )
        
        connectionManager = BluetoothConnectionManager(
            context = context,
            scope = scope,
            gattCallback = gattHandler
        )
    }

    override fun init(
        deviceOperationHandler: DeviceOperationHandler,
        permissionHandler: PermissionHandler,
        intentLauncher: IntentLauncher,
        receiverManager: ReceiverManager,
        stateChecker: BluetoothStateChecker
    ) {
        this.deviceOperationHandler = deviceOperationHandler
        this.receiverManager = receiverManager
        
        scanManager = BluetoothScanManager(
            deviceOperationHandler = deviceOperationHandler,
            permissionHandler = permissionHandler,
            intentLauncher = intentLauncher,
            stateChecker = stateChecker,
            onDeviceDiscovered = deviceRepository::updateDeviceStatus,
            onScanResultCallback = { scanResultCallback?.invoke(it) }
        )
        
        initializeBluetoothReceiver()
        deviceRepository.loadSavedDevices()
    }

    override fun clearResources() {
        scope.cancel()
        stopConnectionService()
        unregisterBluetoothReceiver()
    }

    override fun connectDevice(bluetoothDevice: BluetoothDevice) {
        when {
            checkConnectedDevice(bluetoothDevice.address) -> disconnect(bluetoothDevice.address)
            else -> {
                setDeviceConnecting(bluetoothDevice.address)
                connectionManager.connectDevice(bluetoothDevice)
            }
        }
    }

    override fun connect(macAddress: String) {
        connectionManager.connect(macAddress) {
            setDeviceConnecting(macAddress)
        }
    }

    override fun disconnect(macAddress: String) {
        connectionManager.disconnect(macAddress)
        gattHandler.resetTimestamp()
    }

    override fun registerBluetoothReceiver() {
        receiverManager.registerReceiver(
            bluetoothReceiver,
            IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        )
    }

    override fun unregisterBluetoothReceiver() {
        runCatching {
            receiverManager.unregisterReceiver(bluetoothReceiver)
        }.onFailure { e ->
            Timber.e(e, "Failed to unregister receiver")
        }
    }

    override fun checkConnectedDevice(macAddress: String): Boolean =
        deviceRepository.isConnected(macAddress)

    override fun startScan() {
        scanManager.startScan()
    }

    override fun onScanResult(callback: (scanResult: ScanResult) -> Unit) {
        this.scanResultCallback = callback
    }

    override fun onDeviceConnected(callback: () -> Unit) {
        this.deviceConnectedCallback = callback
    }

    override fun onDeviceDisconnected(callback: () -> Unit) {
        this.deviceDisconnectedCallback = callback
    }

    override fun onDeviceDataReceived(callback: () -> Unit) {
        this.deviceDataReceivedCallback = callback
    }

    override fun onBluetoothStateChanged(callback: (isEnabled: Boolean) -> Unit) {
        this.bluetoothStateChangedCallback = callback
    }

    override fun observeDeviceConnectionStatus(): StateFlow<Map<String, DeviceConnectionStatus>> =
        deviceRepository.deviceConnectionStatusList

    // Private functions

    private fun initializeBluetoothReceiver() {
        bluetoothReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.STATE_OFF)
                handleBluetoothStateChange(state)
            }
        }
    }

    private fun handleBluetoothStateChange(state: Int) {
        when (state) {
            BluetoothAdapter.STATE_ON -> {
                startScan()
                bluetoothStateChangedCallback?.invoke(true)
            }
            BluetoothAdapter.STATE_OFF -> {
                stopConnectionService()
                scanManager.stopScan()
                bluetoothStateChangedCallback?.invoke(false)
            }
        }
    }

    private fun setDeviceConnecting(macAddress: String) {
        val currentStatus = deviceRepository.getDeviceStatus(macAddress) ?: return
        val updatedStatus = DeviceConnectionStatus.connecting(currentStatus.device)
        deviceRepository.updateDeviceStatus(macAddress, updatedStatus)
    }

    private fun handleDeviceConnected(gatt: BluetoothGatt) {
        connectionManager.addConnectedDevice(gatt.device.address, gatt)
        
        val currentStatus = deviceRepository.getDeviceStatus(gatt.device.address) ?: return
        val updatedStatus = DeviceConnectionStatus.connected(currentStatus.device)
        
        deviceRepository.updateDeviceStatus(gatt.device.address, updatedStatus)
        deviceRepository.saveNewDevice(currentStatus.device)
        
        gatt.discoverServices()
        deviceConnectedCallback?.invoke()
        startConnectionServiceIfNeeded()
    }

    private fun handleDeviceDisconnected(gatt: BluetoothGatt) {
        val currentStatus = deviceRepository.getDeviceStatus(gatt.device.address) ?: return
        val updatedStatus = DeviceConnectionStatus.disconnected(currentStatus.device)
        
        deviceRepository.updateDeviceStatus(gatt.device.address, updatedStatus)
        
        connectionManager.closeConnection(gatt)
        deviceDisconnectedCallback?.invoke()
        stopConnectionService()
    }

    private fun startConnectionServiceIfNeeded() {
        if (!isServiceRunning(DeviceConnectionService::class.java)) {
            deviceOperationHandler.startConnectionService(DeviceConnectionService::class.java)
        }
    }

    private fun stopConnectionService() {
        deviceOperationHandler.stopConnectionService(DeviceConnectionService::class.java)
    }
}
