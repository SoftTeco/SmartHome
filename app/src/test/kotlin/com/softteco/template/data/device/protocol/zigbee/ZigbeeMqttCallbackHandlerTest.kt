package com.softteco.template.data.device.protocol.zigbee

import com.softteco.template.BaseTest
import com.softteco.template.data.device.ThermometerRepository
import com.softteco.template.utils.MainDispatcherExtension
import io.kotest.matchers.shouldBe
import io.mockk.coVerify
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MainDispatcherExtension::class)
class ZigbeeMqttCallbackHandlerTest : BaseTest() {

    @RelaxedMockK
    private lateinit var thermometerRepository: ThermometerRepository

    @RelaxedMockK
    private lateinit var deviceRepository: ZigbeeDeviceRepository

    @RelaxedMockK
    private lateinit var scanManager: ZigbeeScanManager

    private lateinit var handler: ZigbeeMqttCallbackHandler
    private lateinit var testScope: TestScope

    private var connectionCompleteCallbackInvoked = false
    private var connectionLostCallbackInvoked = false
    private var deviceDataReceivedCallbackInvoked = false
    private var lastReconnectValue = false

    @BeforeEach
    fun setup() {
        testScope = TestScope(UnconfinedTestDispatcher())
        connectionCompleteCallbackInvoked = false
        connectionLostCallbackInvoked = false
        deviceDataReceivedCallbackInvoked = false
        lastReconnectValue = false

        val config = MqttCallbackConfig(
            thermometerRepository = thermometerRepository,
            deviceRepository = deviceRepository,
            scope = testScope,
            scanManager = scanManager,
            onConnectionComplete = { reconnect ->
                connectionCompleteCallbackInvoked = true
                lastReconnectValue = reconnect
            },
            onConnectionLost = { connectionLostCallbackInvoked = true },
            onDeviceDataReceived = { deviceDataReceivedCallbackInvoked = true }
        )

        handler = ZigbeeMqttCallbackHandler(config)
    }

    @Test
    fun `connectComplete should invoke callback with reconnect status`() {
        runTest {
            // When
            handler.connectComplete(true, "tcp://test-server:1883")

            // Then
            connectionCompleteCallbackInvoked shouldBe true
            lastReconnectValue shouldBe true
        }
    }

    @Test
    fun `connectComplete should handle first connection`() {
        runTest {
            // When
            handler.connectComplete(false, "tcp://test-server:1883")

            // Then
            connectionCompleteCallbackInvoked shouldBe true
            lastReconnectValue shouldBe false
        }
    }

    @Test
    fun `connectionLost should disconnect all devices and invoke callback`() {
        runTest {
            // When
            handler.connectionLost(Throwable("Connection error"))

            // Then
            verify(exactly = 1) { deviceRepository.disconnectAllDevices() }
            connectionLostCallbackInvoked shouldBe true
        }
    }

    @Test
    fun `connectionLost should handle null cause`() {
        runTest {
            // When
            handler.connectionLost(null)

            // Then
            verify(exactly = 1) { deviceRepository.disconnectAllDevices() }
            connectionLostCallbackInvoked shouldBe true
        }
    }

    @Test
    fun `messageArrived should handle device discovery message`() {
        runTest {
            // Given
            val topic = "zigbee2mqtt/bridge/devices"
            val payload = """
                [
                    {
                        "friendly_name": "device1",
                        "ieee_address": "0xaabbccddeeff1122"
                    }
                ]
            """.trimIndent()
            val message = MqttMessage(payload.toByteArray())

            // When
            handler.messageArrived(topic, message)

            // Then
            verify(exactly = 1) { scanManager.handleDeviceDiscoveryMessage(message) }
        }
    }

    @Test
    fun `messageArrived should handle unknown topic gracefully`() {
        runTest {
            // Given
            val topic = "unknown/topic/path"
            val payload = "test data"
            val message = MqttMessage(payload.toByteArray())

            // When
            handler.messageArrived(topic, message)

            // Then - should not crash, just log warning
            verify(exactly = 0) { scanManager.handleDeviceDiscoveryMessage(any()) }
            coVerify(exactly = 0) { thermometerRepository.saveCurrentMeasurement(any()) }
        }
    }
}
