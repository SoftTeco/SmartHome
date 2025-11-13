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
 * Configuration data for ZigbeeMqttCallbackHandler.
 */
internal data class MqttCallbackConfig(
    val thermometerRepository: ThermometerRepository,
    val deviceRepository: ZigbeeDeviceRepository,
    val scope: CoroutineScope,
    val scanManager: ZigbeeScanManager,
    val onConnectionComplete: (Boolean) -> Unit,
    val onConnectionLost: () -> Unit,
    val onDeviceDataReceived: () -> Unit
)

/**
 * Handles MQTT callbacks for connection state and data reception.
 */
internal class ZigbeeMqttCallbackHandler(
    config: MqttCallbackConfig
) : MqttCallbackExtended {
    private val thermometerRepository = config.thermometerRepository
    private val deviceRepository = config.deviceRepository
    private val scope = config.scope
    private val scanManager = config.scanManager
    private val onConnectionComplete = config.onConnectionComplete
    private val onConnectionLost = config.onConnectionLost
    private val onDeviceDataReceived = config.onDeviceDataReceived

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
        } catch (e: org.json.JSONException) {
            Timber.e(e, "Failed to parse device data message")
        } catch (e: IllegalArgumentException) {
            Timber.e(e, "Invalid device data format")
        }
    }
}
