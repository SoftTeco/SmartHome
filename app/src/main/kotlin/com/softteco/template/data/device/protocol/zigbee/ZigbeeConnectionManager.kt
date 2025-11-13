package com.softteco.template.data.device.protocol.zigbee

import android.content.Context
import com.softteco.template.BuildConfig
import com.softteco.template.Constants.ZIGBEE_BUFFER_SIZE
import com.softteco.template.utils.ZigbeeTopic
import dagger.hilt.android.qualifiers.ApplicationContext
import info.mqtt.android.service.MqttAndroidClient
import info.mqtt.android.service.QoS
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions
import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import timber.log.Timber
import java.util.UUID

/**
 * Manages MQTT connection and subscription operations.
 */
internal class ZigbeeConnectionManager(
    @ApplicationContext private val context: Context,
    private val mqttCallback: ZigbeeMqttCallbackHandler
) {
    private var mqttClient: MqttAndroidClient? = null
    private var connectedToHub: Boolean = false
    private val subscribedTopics = mutableSetOf<String>()

    fun initializeClient() {
        mqttClient = MqttAndroidClient(
            context,
            BuildConfig.ZIGBEE_SERVER_URL_VALUE,
            UUID.randomUUID().toString()
        )
        mqttClient?.setCallback(mqttCallback)
    }

    fun connectToHub(
        topic: String,
        onSuccess: () -> Unit,
        onFailure: () -> Unit
    ) {
        val options = MqttConnectOptions().apply {
            isAutomaticReconnect = true
            isCleanSession = false
        }

        mqttClient?.connect(
            options,
            null,
            object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken) {
                    configureDisconnectedBuffer()
                    connectedToHub = true
                    startDeviceScan()
                    subscribeToTopic(topic, onSuccess = onSuccess, onFailure = onFailure)
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    connectedToHub = false
                    Timber.e(exception, "Failed to connect to MQTT hub")
                    onFailure()
                }
            }
        )
    }

    fun subscribeToTopic(
        topic: String,
        onSuccess: () -> Unit,
        onFailure: () -> Unit
    ) {
        if (subscribedTopics.contains(topic)) {
            Timber.d("Already subscribed to topic: $topic")
            return
        }

        mqttClient?.subscribe(
            topic,
            QoS.AtMostOnce.value,
            null,
            object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken) {
                    subscribedTopics.add(topic)
                    onSuccess()
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Timber.e(exception, "Failed to subscribe to topic: $topic")
                    onFailure()
                }
            }
        )
    }

    fun unsubscribeFromTopic(
        topic: String,
        onSuccess: () -> Unit,
        onFailure: () -> Unit
    ) {
        mqttClient?.unsubscribe(
            topic,
            null,
            object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken) {
                    subscribedTopics.remove(topic)
                    onSuccess()
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Timber.e(exception, "Failed to unsubscribe from topic: $topic")
                    onFailure()
                }
            }
        )
    }

    fun disconnect() {
        mqttClient?.disconnect()
        connectedToHub = false
        subscribedTopics.clear()
    }

    fun isConnectedToHub(): Boolean = connectedToHub

    fun isSubscribedToTopic(topic: String): Boolean = subscribedTopics.contains(topic)

    private fun startDeviceScan() {
        val scanTopic = ZigbeeTopic.ZIGBEE_DEVICE_TOPIC.value

        mqttClient?.subscribe(
            scanTopic,
            QoS.AtMostOnce.value
        ) { _, _ ->
            // Message will be handled by mqttCallback
        }
    }

    private fun configureDisconnectedBuffer() {
        val options = DisconnectedBufferOptions().apply {
            isBufferEnabled = true
            bufferSize = ZIGBEE_BUFFER_SIZE
            isPersistBuffer = false
            isDeleteOldestMessages = false
        }
        mqttClient?.setBufferOpts(options)
    }
}
