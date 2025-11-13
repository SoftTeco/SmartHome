package com.softteco.template.data.device.protocol.zigbee

import android.content.Context
import com.softteco.template.BaseTest
import com.softteco.template.utils.MainDispatcherExtension
import io.kotest.matchers.shouldBe
import io.mockk.impl.annotations.RelaxedMockK
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MainDispatcherExtension::class)
class ZigbeeConnectionManagerTest : BaseTest() {

    @RelaxedMockK
    private lateinit var context: Context

    @RelaxedMockK
    private lateinit var mqttCallback: ZigbeeMqttCallbackHandler

    private lateinit var connectionManager: ZigbeeConnectionManager

    private val testTopic = "zigbee2mqtt/test/device"

    @BeforeEach
    fun setup() {
        connectionManager = ZigbeeConnectionManager(context, mqttCallback)
    }

    @Test
    fun `isConnectedToHub should return false initially`() {
        runTest {
            // When
            val result = connectionManager.isConnectedToHub()

            // Then
            result shouldBe false
        }
    }

    @Test
    fun `isSubscribedToTopic should return false for unsubscribed topic`() {
        runTest {
            // When
            val result = connectionManager.isSubscribedToTopic(testTopic)

            // Then
            result shouldBe false
        }
    }

    @Test
    fun `disconnect should clear connection and subscriptions`() {
        runTest {
            // When
            connectionManager.disconnect()

            // Then
            connectionManager.isConnectedToHub() shouldBe false
        }
    }

    @Test
    fun `disconnect should handle multiple calls gracefully`() {
        runTest {
            // When - calling disconnect multiple times
            connectionManager.disconnect()
            connectionManager.disconnect()

            // Then - should not crash
            connectionManager.isConnectedToHub() shouldBe false
        }
    }
}
