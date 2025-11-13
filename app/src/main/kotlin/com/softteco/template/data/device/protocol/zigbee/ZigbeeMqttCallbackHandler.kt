package com.softteco.template.data.device.protocol.zigbee

import com.softteco.template.data.device.ThermometerRepository
import com.softteco.template.data.device.ThermometerValues
import com.softteco.template.utils.ZigbeeTopic
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.json.JSONObject
import timber.log.Timber
import java.time.LocalDateTime

/**
 * Handles MQTT callbacks for connection state and data reception.
 */
internal class ZigbeeMqttCallbackHandler(
    private val thermometerRepository: ThermometerRepository,
    private val deviceRepository: ZigbeeDeviceRepository,
    private val scope: CoroutineScope,
    private val scanManager: ZigbeeScanManager,
    private val onConnectionComplete: (Boolean) -> Unit,
    private val onConnectionLost: () -> Unit,
    private val onDeviceDataReceived: () -> Unit
) : MqttCallbackExtended {

    override fun connectComplete(reconnect: Boolean, serverURI: String) {
        Timber.d("MQTT connection complete. Reconnect: $reconnect, URI: $serverURI")
        onConnectionComplete(reconnect)
    }

    override fun connectionLost(cause: Throwable?) {
        Timber.e(cause, "MQTT connection lost")
        deviceRepository.disconnectAllDevices()
        onConnectionLost()
    }

    override fun messageArrived(topic: String, message: MqttMessage) {
        when {
            topic.contains(ZigbeeTopic.ZIGBEE_DEVICE_TOPIC.value) -> {
                handleDeviceDiscoveryMessage(message)
            }
            topic.contains(ZigbeeTopic.ZIGBEE_DATA_TOPIC.value) -> {
                handleDeviceDataMessage(topic, message)
            }
            else -> {
                Timber.w("Received message from unknown topic: $topic")
            }
        }
    }

    override fun deliveryComplete(token: IMqttDeliveryToken) {
        // Not used for incoming messages
    }

    private fun handleDeviceDiscoveryMessage(message: MqttMessage) {
        scanManager.handleDeviceDiscoveryMessage(message)
    }

    private fun handleDeviceDataMessage(topic: String, message: MqttMessage) {
        try {
            val macAddress = topic.split("/")[1]
            val jsonData = JSONObject(String(message.payload))
            
            val thermometerData = ThermometerValues.DataLYWSD03MMC(
                temperature = jsonData.getDouble("temperature"),
                humidity = jsonData.getInt("humidity"),
                battery = jsonData.getInt("battery").toDouble(),
                macAddress = macAddress,
                timestamp = LocalDateTime.now()
            )
            
            scope.launch(Dispatchers.IO) {
                thermometerRepository.saveCurrentMeasurement(thermometerData)
            }
            
            onDeviceDataReceived()
        } catch (e: Exception) {
            Timber.e(e, "Failed to parse device data message")
        }
    }
}

