package com.softteco.template.data.device.protocol.zigbee

import android.content.Intent
import com.softteco.template.BuildConfig
import com.softteco.template.MainActivity
import com.softteco.template.data.base.error.Result
import com.softteco.template.data.device.Device
import com.softteco.template.data.device.ProtocolType
import com.softteco.template.data.device.ThermometerData
import com.softteco.template.data.zigbee.ZigbeeState
import com.softteco.template.data.device.ThermometerRepository
import com.softteco.template.data.device.ThermometerValues
import com.softteco.template.data.zigbee.ZigbeeHelper
import com.softteco.template.utils.ZigbeeDevice
import com.softteco.template.utils.ZigbeeTopic
import com.softteco.template.utils.parseZigbeeDevices
import com.softteco.template.utils.protocol.DeviceConnectionStatus
import com.softteco.template.utils.protocol.DeviceConnectionService
import com.softteco.template.utils.protocol.checkRemainingConnectionForService
import com.softteco.template.utils.protocol.getDeviceImage
import com.softteco.template.utils.protocol.getDeviceModel
import com.softteco.template.utils.protocol.isServiceRunning
import info.mqtt.android.service.MqttAndroidClient
import info.mqtt.android.service.QoS
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions
import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.json.JSONObject
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class ZigbeeHelperImpl @Inject constructor(
    private val thermometerRepository: ThermometerRepository
) : ZigbeeHelper, ZigbeeState {

    private var activity: MainActivity? = null
    private var mqttAndroidClient: MqttAndroidClient? = null
    private var connectedToHub: Boolean = false
    override var onConnect: (() -> Unit)? = null
    override var onDisconnect: (() -> Unit)? = null
    override var onScanResult: ((device: ZigbeeDevice) -> Unit)? = null
    override var onDeviceResult: (() -> Unit)? = null
    override var onSubscribed: (() -> Unit)? = null
    override var onUnsubscribed: (() -> Unit)? = null

    private val _deviceConnectionStatusList =
        MutableStateFlow<Map<String, DeviceConnectionStatus>>(emptyMap())
    private val deviceConnectionStatusList: StateFlow<Map<String, DeviceConnectionStatus>> =
        _deviceConnectionStatusList
    private var savedZigBeeDevices = mutableListOf<Device>()

    override fun init(activity: MainActivity) {
        this.activity = activity
        mqttAndroidClient = MqttAndroidClient(
            activity.applicationContext,
            BuildConfig.ZIGBEE_SERVER_URL_VALUE,
            UUID.randomUUID().toString()
        )
        setCallbacks()
        runBlocking {
            withContext(Dispatchers.IO) {
                when (val result = thermometerRepository.getDevices()) {
                    is Result.Success -> {
                        result.data.filter { it.protocolType == ProtocolType.ZIGBEE }.let {
                            savedZigBeeDevices.addAll(it)
                            it.forEach {
                                _deviceConnectionStatusList.update { currentMap ->
                                    currentMap.toMutableMap().apply {
                                        this[it.macAddress] = DeviceConnectionStatus(it, false)
                                    }
                                }
                            }
                        }
                    }

                    is Result.Error -> {}
                }
            }
        }
    }

    override fun connect(topic: String) {
        val mqttConnectOptions = MqttConnectOptions()
        mqttConnectOptions.isAutomaticReconnect = true
        mqttConnectOptions.isCleanSession = false

        mqttAndroidClient?.connect(mqttConnectOptions, null, object : IMqttActionListener {
            override fun onSuccess(asyncActionToken: IMqttToken) {
                val disconnectedBufferOptions = DisconnectedBufferOptions()
                disconnectedBufferOptions.isBufferEnabled = true
                disconnectedBufferOptions.bufferSize = 100
                disconnectedBufferOptions.isPersistBuffer = false
                disconnectedBufferOptions.isDeleteOldestMessages = false
                mqttAndroidClient?.setBufferOpts(disconnectedBufferOptions)
                connectedToHub = true
                subscribeToTopic(topic)
            }

            override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                connectedToHub = false
                onDisconnect?.invoke()
            }
        })
    }

    override fun drop() {
        stopService()
        mqttAndroidClient?.disconnect()
        this.activity = null
    }

    override suspend fun provideConnectionToDevice(topic: String) {
        if (checkConnectedDevice(topic.split("/")[1])) {
            unsubscribeFromTopic(topic)
        } else {
            subscribeToTopic(topic)
        }
    }

    override fun provideConnectionToDeviceViaMacAddress(macAddress: String) {
        if(connectedToHub) {
            subscribeToTopic(ZigbeeTopic.ZIGBEE_DATA_TOPIC.value.plus(macAddress))
        } else {
            connect(ZigbeeTopic.ZIGBEE_DATA_TOPIC.value.plus(macAddress))
        }
    }

    override fun onScanCallback(onScanResult: (device: ZigbeeDevice) -> Unit) {
        this.onScanResult = onScanResult
    }

    override fun onDeviceResultCallback(onDeviceResult: () -> Unit) {
        this.onDeviceResult = onDeviceResult
    }

    override fun getObservableDeviceConnectionStatusList() = deviceConnectionStatusList

    private fun setCallbacks() {
        mqttAndroidClient?.setCallback(object : MqttCallbackExtended {
            override fun connectComplete(reconnect: Boolean, serverURI: String) {
                if (reconnect) {
                    onConnect?.invoke()
                } else {
                    onConnect?.invoke()
                }
            }

            override fun connectionLost(cause: Throwable?) {
                onDisconnect?.invoke()
                stopService()
                _deviceConnectionStatusList.update { currentMap ->
                    currentMap.mapValues { (_, status) ->
                        if (status.device.protocolType == ProtocolType.ZIGBEE) {
                            status.copy(isConnected = false)
                        } else {
                            status
                        }
                    }
                }
            }

            override fun messageArrived(topic: String, message: MqttMessage) {}

            override fun deliveryComplete(token: IMqttDeliveryToken) {}
        })
    }

    private fun subscribeToTopic(topic: String, deviceName: String = "") {
        mqttAndroidClient?.subscribe(
            topic,
            QoS.AtMostOnce.value,
            null,
            object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken) {
                    when {
                        topic.contains(ZigbeeTopic.ZIGBEE_DEVICE_TOPIC.value) -> {

                        }

                        topic.contains(ZigbeeTopic.ZIGBEE_DATA_TOPIC.value.plus(deviceName)) -> {
                            provideConnectedState(asyncActionToken.topics[0].split("/")[1])
                            if (!isServiceRunning(activity, DeviceConnectionService::class.java)) {
                                activity?.startForegroundService(
                                    Intent(
                                        activity,
                                        DeviceConnectionService::class.java
                                    )
                                )
                            }
                        }
                    }
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    onUnsubscribed?.invoke()
                }
            }
        )

        mqttAndroidClient?.subscribe(
            topic,
            QoS.AtMostOnce.value
        ) { receivedTopic, message ->
            val macAddress = receivedTopic.split("/")[1]
            when (receivedTopic) {
                ZigbeeTopic.ZIGBEE_DATA_TOPIC.value.plus(macAddress) -> {
                    onDeviceResult?.invoke()
                    JSONObject(String(message.payload)).let {
                        runBlocking {
                            withContext(Dispatchers.IO) {
                                thermometerRepository.saveCurrentMeasurement(
                                    ThermometerValues.DataLYWSD03MMC(
                                        it["temperature"] as Double,
                                        (it["humidity"] as Double).toInt(),
                                        (it["battery"] as Int).toDouble(),
                                        macAddress,
                                        LocalDateTime.now(),
                                    )
                                )
                            }
                        }
                    }
                }

                ZigbeeTopic.ZIGBEE_DEVICE_TOPIC.value -> {
                    val devicesJson = String(message.payload)
                    val devices = parseZigbeeDevices(devicesJson)
                    devices.forEach { device ->
                        device.modelId?.let {
                            _deviceConnectionStatusList.update { currentMap ->
                                currentMap.toMutableMap().apply {
                                    this[device.ieeeAddress] = DeviceConnectionStatus(
                                        Device.Basic(
                                            type = Device.Type.TemperatureAndHumidity,
                                            family = Device.Family.Sensor,
                                            model = activity?.getDeviceModel(device.modelId)
                                                ?: Device.Model.Unknown,
                                            id = UUID.randomUUID(),
                                            defaultName = device.modelId,
                                            name = "Temperature and Humidity Monitor",
                                            macAddress = device.ieeeAddress,
                                            img = activity?.getDeviceImage(device.modelId),
                                            location = "",
                                            protocolType = ProtocolType.ZIGBEE
                                        ),
                                        false
                                    )
                                }
                            }
                            onScanResult?.invoke(device)
                        }
                    }
                }

                else -> {}
            }
        }
    }

    private fun unsubscribeFromTopic(topic: String) {
        mqttAndroidClient?.unsubscribe(topic, null, object : IMqttActionListener {
            override fun onSuccess(asyncActionToken: IMqttToken) {
                when {
                    topic.contains(ZigbeeTopic.ZIGBEE_DEVICE_TOPIC.value) -> {

                    }

                    topic.contains(ZigbeeTopic.ZIGBEE_DATA_TOPIC.value) -> {
                        val macAddress = topic.split("/")[1]
                        _deviceConnectionStatusList.update { currentMap ->
                            currentMap.toMutableMap().apply {
                                this[macAddress]?.let { status ->
                                    val updatedStatus = status.copy(isConnected = false)
                                    this[macAddress] = updatedStatus
                                }
                            }
                        }
                        stopService()
                    }
                }
            }

            override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                onUnsubscribed?.invoke()
            }
        })
    }

    private fun provideConnectedState(deviceAddress: String) {
        _deviceConnectionStatusList.update { currentMap ->
            currentMap.toMutableMap().apply {
                this[deviceAddress]?.let { status ->
                    val updatedStatus = status.copy(isConnected = true)
                    this[deviceAddress] = updatedStatus
                    if (savedZigBeeDevices.none { o -> o.macAddress == deviceAddress }) {
                        runBlocking {
                            withContext(Dispatchers.IO) {
                                thermometerRepository.saveDevice(status.device)
                                thermometerRepository.saveThermometerData(
                                    ThermometerData(
                                        deviceId = status.device.id,
                                        deviceName = status.device.name,
                                        macAddress = status.device.macAddress
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
        onConnect?.invoke()
    }

    private suspend fun checkConnectedDevice(topic: String): Boolean {
        val statusMap = deviceConnectionStatusList.first()
        return statusMap[topic]?.isConnected ?: false
    }

    private fun stopService() {
        if (!checkRemainingConnectionForService(
                activity?.bluetoothHelper?.getObservableDeviceConnectionStatusList(),
                getObservableDeviceConnectionStatusList()
            )
        ) {
            activity?.stopService(
                Intent(
                    activity,
                    DeviceConnectionService::class.java
                )
            )
        }
    }
}
