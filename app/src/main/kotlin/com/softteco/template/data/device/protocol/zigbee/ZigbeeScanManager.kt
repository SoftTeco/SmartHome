package com.softteco.template.data.device.protocol.zigbee

import com.softteco.template.data.device.Device
import com.softteco.template.data.device.ProtocolType
import com.softteco.template.data.device.protocol.common.DeviceOperationHandler
import com.softteco.template.utils.ZigbeeDevice
import com.softteco.template.utils.parseZigbeeDevices
import com.softteco.template.utils.protocol.DeviceConnectionStatus
import org.eclipse.paho.client.mqttv3.MqttMessage
import timber.log.Timber
import java.util.UUID

/**
 * Manages Zigbee device scanning operations through MQTT.
 */
internal class ZigbeeScanManager(
    private val deviceOperationHandler: DeviceOperationHandler,
    private val onDeviceDiscovered: (String, DeviceConnectionStatus) -> Unit,
    private val onScanResultCallback: ((ZigbeeDevice) -> Unit)?
) {
    private companion object {
        const val DEFAULT_DEVICE_NAME = "Temperature and Humidity Monitor"
    }

    fun handleDeviceDiscoveryMessage(message: MqttMessage) {
        try {
            val devicesJson = String(message.payload)
            val devices = parseZigbeeDevices(devicesJson)
            
            devices.forEach { device ->
                device.modelId?.let { modelId ->
                    val createdDevice = createDevice(device.ieeeAddress, modelId)
                    val status = DeviceConnectionStatus.searching(createdDevice)
                    
                    onDeviceDiscovered(device.ieeeAddress, status)
                    onScanResultCallback?.invoke(device)
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to parse device discovery message")
        }
    }

    private fun createDevice(ieeeAddress: String, modelId: String): Device.Basic {
        return Device.Basic(
            type = Device.Type.TemperatureAndHumidity,
            family = Device.Family.Sensor,
            model = deviceOperationHandler.getDeviceModel(modelId),
            id = UUID.randomUUID(),
            defaultName = modelId,
            name = DEFAULT_DEVICE_NAME,
            macAddress = ieeeAddress,
            img = deviceOperationHandler.getDeviceImage(modelId),
            location = "",
            protocolType = ProtocolType.ZIGBEE
        )
    }
}
