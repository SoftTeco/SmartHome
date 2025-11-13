package com.softteco.template.data.zigbee

import com.softteco.template.data.device.protocol.common.DeviceOperationHandler
import com.softteco.template.utils.ZigbeeDevice
import com.softteco.template.utils.protocol.DeviceConnectionStatus
import kotlinx.coroutines.flow.StateFlow

interface ZigbeeHelper {
    /**
     * Initialize the helper with necessary handlers.
     * This separates concerns and removes direct Activity dependency.
     * 
     * @param deviceOperationHandler Handler for device-specific operations
     */
    fun init(
        deviceOperationHandler: DeviceOperationHandler
    )

    /**
     * Clean up resources and clear the helper when the activity is destroyed.
     */
    fun clearResources()

    /**
     * Connect to the MQTT server and subscribe to the topic.
     */
    fun connectToHub(topic: String)

    /**
     * Connect to ZigBee device.
     */
    fun connect(topic: String)

    /**
     * Disconnect from ZigBee device.
     */
    fun disconnect(topic: String)

    /**
     * Connect to the ZigBee device via mac address.
     */
    fun connectViaMacAddress(macAddress: String)

    /**
     * Set callback to receive scan results for discoverable ZigBee devices.
     */
    fun onScanResult(callback: (device: ZigbeeDevice) -> Unit)

    /**
     * Set callback to receive data from the MQTT server.
     */
    fun onDeviceDataReceived(callback: () -> Unit)

    /**
     * Set callback when device is connected.
     */
    fun onDeviceConnected(callback: () -> Unit)

    /**
     * Set callback when device is disconnected.
     */
    fun onDeviceDisconnected(callback: () -> Unit)

    /**
     * Observe connection statuses of known ZigBee devices.
     */
    fun observeDeviceConnectionStatus(): StateFlow<Map<String, DeviceConnectionStatus>>

    /**
     * Checking the device connection status.
     */
    fun checkConnectedDevice(topic: String): Boolean
}
