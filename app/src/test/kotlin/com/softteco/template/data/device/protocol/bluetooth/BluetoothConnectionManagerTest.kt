package com.softteco.template.data.device.protocol.bluetooth

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.content.Context
import com.softteco.template.BaseTest
import com.softteco.template.utils.MainDispatcherExtension
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.justRun
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MainDispatcherExtension::class)
class BluetoothConnectionManagerTest : BaseTest() {

    @RelaxedMockK
    private lateinit var context: Context

    @RelaxedMockK
    private lateinit var gattCallback: BluetoothGattCallbackHandler

    @MockK
    private lateinit var bluetoothGatt: BluetoothGatt

    @MockK
    private lateinit var bluetoothDevice: BluetoothDevice

    private lateinit var connectionManager: BluetoothConnectionManager
    private lateinit var testScope: TestScope

    private val testMacAddress = "AA:BB:CC:DD:EE:FF"
    private val testMacAddress2 = "11:22:33:44:55:66"

    @BeforeEach
    fun setup() {
        testScope = TestScope(UnconfinedTestDispatcher())
        connectionManager = BluetoothConnectionManager(context, testScope, gattCallback)

        every { bluetoothDevice.address } returns testMacAddress
        every { bluetoothGatt.device } returns bluetoothDevice
        justRun { bluetoothGatt.disconnect() }
        justRun { bluetoothGatt.close() }
    }

    @Test
    fun `isConnected should return false when no devices connected`() {
        runTest {
            // When
            val result = connectionManager.isConnected(testMacAddress)

            // Then
            result shouldBe false
        }
    }

    @Test
    fun `isConnected should return true after device added`() {
        runTest {
            // Given
            connectionManager.addConnectedDevice(testMacAddress, bluetoothGatt)

            // When
            val result = connectionManager.isConnected(testMacAddress)

            // Then
            result shouldBe true
        }
    }

    @Test
    fun `isConnected should return false for different mac address`() {
        runTest {
            // Given
            connectionManager.addConnectedDevice(testMacAddress, bluetoothGatt)

            // When
            val result = connectionManager.isConnected(testMacAddress2)

            // Then
            result shouldBe false
        }
    }

    @Test
    fun `addConnectedDevice should store device`() {
        runTest {
            // When
            connectionManager.addConnectedDevice(testMacAddress, bluetoothGatt)

            // Then
            connectionManager.isConnected(testMacAddress) shouldBe true
        }
    }

    @Test
    fun `addConnectedDevice should replace existing device with same mac address`() {
        runTest {
            // Given
            val gatt1: BluetoothGatt = bluetoothGatt
            val gatt2: BluetoothGatt = bluetoothGatt
            connectionManager.addConnectedDevice(testMacAddress, gatt1)

            // When
            connectionManager.addConnectedDevice(testMacAddress, gatt2)

            // Then
            connectionManager.isConnected(testMacAddress) shouldBe true
        }
    }

    @Test
    fun `removeConnectedDevice should remove device from map`() {
        runTest {
            // Given
            connectionManager.addConnectedDevice(testMacAddress, bluetoothGatt)
            connectionManager.isConnected(testMacAddress) shouldBe true

            // When
            connectionManager.removeConnectedDevice(testMacAddress)

            // Then
            connectionManager.isConnected(testMacAddress) shouldBe false
        }
    }

    @Test
    fun `removeConnectedDevice should not affect other devices`() {
        runTest {
            // Given
            val gatt2: BluetoothGatt = bluetoothGatt
            connectionManager.addConnectedDevice(testMacAddress, bluetoothGatt)
            connectionManager.addConnectedDevice(testMacAddress2, gatt2)

            // When
            connectionManager.removeConnectedDevice(testMacAddress)

            // Then
            connectionManager.isConnected(testMacAddress) shouldBe false
            connectionManager.isConnected(testMacAddress2) shouldBe true
        }
    }

    @Test
    fun `disconnect should call disconnect on gatt when device is connected`() {
        runTest {
            // Given
            connectionManager.addConnectedDevice(testMacAddress, bluetoothGatt)

            // When
            connectionManager.disconnect(testMacAddress)

            // Then
            verify(exactly = 1) { bluetoothGatt.disconnect() }
        }
    }

    @Test
    fun `disconnect should not crash when device is not connected`() {
        runTest {
            // When - calling disconnect on non-existent device
            connectionManager.disconnect(testMacAddress)

            // Then - should not crash (null safety)
            verify(exactly = 0) { bluetoothGatt.disconnect() }
        }
    }

    @Test
    fun `closeConnection should close gatt and remove device`() {
        runTest {
            // Given
            connectionManager.addConnectedDevice(testMacAddress, bluetoothGatt)
            connectionManager.isConnected(testMacAddress) shouldBe true

            // When
            connectionManager.closeConnection(bluetoothGatt)

            // Then
            verify(exactly = 1) { bluetoothGatt.close() }
            connectionManager.isConnected(testMacAddress) shouldBe false
        }
    }

    @Test
    fun `multiple devices can be connected simultaneously`() {
        runTest {
            // Given
            val gatt2: BluetoothGatt = bluetoothGatt

            // When
            connectionManager.addConnectedDevice(testMacAddress, bluetoothGatt)
            connectionManager.addConnectedDevice(testMacAddress2, gatt2)

            // Then
            connectionManager.isConnected(testMacAddress) shouldBe true
            connectionManager.isConnected(testMacAddress2) shouldBe true
        }
    }

    @Test
    fun `disconnect should work with multiple connected devices`() {
        runTest {
            // Given
            val gatt2: BluetoothGatt = bluetoothGatt
            connectionManager.addConnectedDevice(testMacAddress, bluetoothGatt)
            connectionManager.addConnectedDevice(testMacAddress2, gatt2)

            // When
            connectionManager.disconnect(testMacAddress)

            // Then
            verify(exactly = 1) { bluetoothGatt.disconnect() }
            // Device should still be in map until closeConnection is called
            connectionManager.isConnected(testMacAddress) shouldBe true
            connectionManager.isConnected(testMacAddress2) shouldBe true
        }
    }
}
