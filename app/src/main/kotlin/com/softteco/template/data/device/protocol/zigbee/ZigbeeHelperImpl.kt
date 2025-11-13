package com.softteco.template.data.device.protocol.zigbee

import android.content.Context
import com.softteco.template.data.bluetooth.BluetoothHelper
import com.softteco.template.data.device.ThermometerRepository
import com.softteco.template.data.device.protocol.common.DeviceOperationHandler
import com.softteco.template.data.zigbee.ZigbeeHelper
import com.softteco.template.data.zigbee.ZigbeeState
import com.softteco.template.utils.ZigbeeDevice
import com.softteco.template.utils.ZigbeeTopic
import com.softteco.template.utils.protocol.DeviceConnectionService
import com.softteco.template.utils.protocol.DeviceConnectionStatus
import com.softteco.template.utils.protocol.checkRemainingConnectionForService
import com.softteco.template.utils.protocol.isServiceRunning
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Main coordinator for Zigbee operations.
 * Delegates responsibilities to specialized managers.
 */
@Singleton
internal class ZigbeeHelperImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val thermometerRepository: ThermometerRepository,
    private val bluetoothHelper: BluetoothHelper
) : ZigbeeHelper, ZigbeeState {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private lateinit var deviceOperationHandler: DeviceOperationHandler

    private val deviceRepository: ZigbeeDeviceRepository
    private lateinit var connectionManager: ZigbeeConnectionManager
    private lateinit var scanManager: ZigbeeScanManager
    private lateinit var mqttHandler: ZigbeeMqttCallbackHandler

    override var deviceConnectedCallback: (() -> Unit)? = null
    override var deviceDisconnectedCallback: (() -> Unit)? = null
    override var scanResultCallback: ((device: ZigbeeDevice) -> Unit)? = null
    override var deviceDataReceivedCallback: (() -> Unit)? = null
    override var subscribedCallback: (() -> Unit)? = null
    override var unsubscribedCallback: (() -> Unit)? = null

    init {
        deviceRepository = ZigbeeDeviceRepository(thermometerRepository, scope)
    }

    override fun init(deviceOperationHandler: DeviceOperationHandler) {
        this.deviceOperationHandler = deviceOperationHandler
        
        initializeManagers()
        deviceRepository.loadSavedDevices()
    }

    override fun clearResources() {
        scope.cancel()
        connectionManager.disconnect()
        stopConnectionService()
    }

    override fun connectToHub(topic: String) {
        connectionManager.connectToHub(
            topic = topic,
            onSuccess = {
                deviceConnectedCallback?.invoke()
            },
            onFailure = {
                deviceDisconnectedCallback?.invoke()
            }
        )
    }

    override fun connect(topic: String) {
        runBlocking {
            val macAddress = topic.split("/")[1]
            if (checkConnectedDevice(macAddress)) {
                disconnect(topic)
            } else {
                setDeviceConnecting(macAddress)
                subscribeToDeviceTopic(topic)
            }
        }
    }

    override fun disconnect(topic: String) {
        connectionManager.unsubscribeFromTopic(
            topic = topic,
            onSuccess = {
                handleDeviceDisconnected(topic)
            },
            onFailure = {
                unsubscribedCallback?.invoke()
            }
        )
    }

    override fun connectViaMacAddress(macAddress: String) {
        val topic = ZigbeeTopic.ZIGBEE_DATA_TOPIC.value + macAddress

        setDeviceConnecting(macAddress)

        if (connectionManager.isConnectedToHub()) {
            subscribeToDeviceTopic(topic)
        } else {
            connectToHub(topic)
        }
    }

    override fun onScanResult(callback: (device: ZigbeeDevice) -> Unit) {
        this.scanResultCallback = callback
    }

    override fun onDeviceDataReceived(callback: () -> Unit) {
        this.deviceDataReceivedCallback = callback
    }

    override fun onDeviceConnected(callback: () -> Unit) {
        this.deviceConnectedCallback = callback
    }

    override fun onDeviceDisconnected(callback: () -> Unit) {
        this.deviceDisconnectedCallback = callback
    }

    override fun observeDeviceConnectionStatus(): StateFlow<Map<String, DeviceConnectionStatus>> =
        deviceRepository.deviceConnectionStatusList

    override fun checkConnectedDevice(topic: String): Boolean {
        val statusMap = runBlocking { deviceRepository.deviceConnectionStatusList.first() }
        return statusMap[topic]?.isConnected ?: false
    }

    override fun removeDeviceFromCache(macAddress: String) {
        deviceRepository.removeDevice(macAddress)
    }

    // Private functions

    private fun initializeManagers() {
        // Initialize scan manager first (without MQTT client reference)
        scanManager = ZigbeeScanManager(
            deviceOperationHandler = deviceOperationHandler,
            onDeviceDiscovered = deviceRepository::updateDeviceStatus,
            onScanResultCallback = scanResultCallback
        )
        
        // Initialize MQTT callback handler
        mqttHandler = ZigbeeMqttCallbackHandler(
            thermometerRepository = thermometerRepository,
            deviceRepository = deviceRepository,
            scope = scope,
            scanManager = scanManager,
            onConnectionComplete = ::handleConnectionComplete,
            onConnectionLost = ::handleConnectionLost,
            onDeviceDataReceived = { deviceDataReceivedCallback?.invoke() }
        )
        
        // Initialize connection manager with callback handler
        connectionManager = ZigbeeConnectionManager(
            context = context,
            mqttCallback = mqttHandler
        )
        
        connectionManager.initializeClient()
    }

    private fun handleConnectionComplete(@Suppress("UNUSED_PARAMETER") reconnect: Boolean) {
        deviceConnectedCallback?.invoke()
    }

    private fun handleConnectionLost() {
        deviceDisconnectedCallback?.invoke()
        stopConnectionService()
    }

    private fun subscribeToDeviceTopic(topic: String) {
        val macAddress = topic.split("/")[1]
        
        connectionManager.subscribeToTopic(
            topic = topic,
            onSuccess = {
                handleDeviceConnected(macAddress)
            },
            onFailure = {
                unsubscribedCallback?.invoke()
            }
        )
    }

    private fun setDeviceConnecting(macAddress: String) {
        val currentStatus = deviceRepository.getDeviceStatus(macAddress) ?: return
        val updatedStatus = DeviceConnectionStatus.connecting(currentStatus.device)
        deviceRepository.updateDeviceStatus(macAddress, updatedStatus)
    }

    private fun handleDeviceConnected(macAddress: String) {
        val currentStatus = deviceRepository.getDeviceStatus(macAddress) ?: run {
            Timber.w("Device not found for address: $macAddress")
            return
        }
        
        val updatedStatus = DeviceConnectionStatus.connected(currentStatus.device)
        deviceRepository.updateDeviceStatus(macAddress, updatedStatus)
        deviceRepository.saveNewDevice(currentStatus.device)
        
        deviceConnectedCallback?.invoke()
        startConnectionServiceIfNeeded()
    }

    private fun handleDeviceDisconnected(topic: String) {
        val macAddress = topic.split("/")[1]
        val currentStatus = deviceRepository.getDeviceStatus(macAddress) ?: return
        
        val updatedStatus = DeviceConnectionStatus.disconnected(currentStatus.device)
        deviceRepository.updateDeviceStatus(macAddress, updatedStatus)
        
        stopConnectionService()
    }

    private fun startConnectionServiceIfNeeded() {
        if (!isServiceRunning(DeviceConnectionService::class.java)) {
            deviceOperationHandler.startConnectionService(DeviceConnectionService::class.java)
        }
    }

    private fun stopConnectionService() {
        // Check if there are any remaining connections before stopping the service
        if (!checkRemainingConnectionForService(
                bluetoothHelper.observeDeviceConnectionStatus(),
                deviceRepository.deviceConnectionStatusList
            )
        ) {
            deviceOperationHandler.stopConnectionService(DeviceConnectionService::class.java)
        }
    }
}
