package com.softteco.template.data.zigbee

import com.softteco.template.MainActivity
import com.softteco.template.utils.ZigbeeDevice
import com.softteco.template.utils.protocol.DeviceConnectionStatus
import kotlinx.coroutines.flow.StateFlow

interface ZigbeeHelper {
    /**
     * Passing the helper class of an Activity instance for the necessary actions when
     * creating an Activity.
     */
    fun init(activity: MainActivity)

    /**
     * Connect to the MQTT server and subscribe to the topic.
     */
    fun connectToHub(topic: String)

    /**
     * Provide connection state to ZigBee device.
     */
    fun connect(topic: String)

    /**
     * Provide disconnection state from ZigBee device.
     */
    fun disconnect(topic: String)

    /**
     * Disconnect from the MQTT server and subscribe to the topic.
     */
    fun drop()

    /**
     * Connecting to the ZigBee device via mac address.
     */
    fun provideConnectionToDeviceViaMacAddress(macAddress: String)

    /**
     * Callback to receive scan results for discoverable ZigBee devices.
     */
    fun onScanCallback(onScanResult: (device: ZigbeeDevice) -> Unit)

    /**
     * Callback to receive data from the MQTT server.
     */
    fun onDeviceResultCallback(onDeviceResult: () -> Unit)

    /**
     * Get observable connection statuses of known ZigBee devices.
     */
    fun getObservableDeviceConnectionStatusList(): StateFlow<Map<String, DeviceConnectionStatus>>

    /**
     * Checking the device connection status.
     */
    fun checkConnectedDevice(topic: String): Boolean
}
