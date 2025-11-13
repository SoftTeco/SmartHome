package com.softteco.template.data.device.protocol.bluetooth

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothProfile
import com.softteco.template.BaseTest
import com.softteco.template.data.bluetooth.BluetoothByteParser
import com.softteco.template.data.device.Device
import com.softteco.template.data.device.ProtocolType
import com.softteco.template.data.device.ThermometerRepository
import com.softteco.template.data.device.ThermometerValues
import com.softteco.template.utils.MainDispatcherExtension
import com.softteco.template.utils.protocol.DeviceConnectionStatus
import io.kotest.matchers.shouldBe
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MainDispatcherExtension::class)
class BluetoothGattCallbackHandlerTest : BaseTest() {

    @RelaxedMockK
    private lateinit var bluetoothByteParser: BluetoothByteParser

    @RelaxedMockK
    private lateinit var thermometerRepository: ThermometerRepository

    @RelaxedMockK
    private lateinit var deviceRepository: BluetoothDeviceRepository

    @MockK
    private lateinit var bluetoothGatt: BluetoothGatt

    @MockK
    private lateinit var bluetoothDevice: BluetoothDevice

    @MockK
    private lateinit var bluetoothGattCharacteristic: BluetoothGattCharacteristic

    private lateinit var handler: BluetoothGattCallbackHandler
    private lateinit var testScope: TestScope

    private var deviceConnectedCallbackInvoked = false
    private var deviceDisconnectedCallbackInvoked = false
    private var deviceDataReceivedCallbackInvoked = false

    private val testDevice = Device.Basic(
        id = java.util.UUID.randomUUID(),
        defaultName = "Test Device",
        name = "Test Device",
        macAddress = "AA:BB:CC:DD:EE:FF",
        img = null,
        location = "",
        type = Device.Type.TemperatureAndHumidity,
        family = Device.Family.Sensor,
        model = Device.Model.LYWSD03MMC,
        protocolType = ProtocolType.BLUETOOTH
    )

    private val testMacAddress = "AA:BB:CC:DD:EE:FF"

    @BeforeEach
    fun setup() {
        testScope = TestScope(UnconfinedTestDispatcher())
        deviceConnectedCallbackInvoked = false
        deviceDisconnectedCallbackInvoked = false
        deviceDataReceivedCallbackInvoked = false

        every { bluetoothGatt.device } returns bluetoothDevice
        every { bluetoothDevice.address } returns testMacAddress

        val config = GattCallbackConfig(
            bluetoothByteParser = bluetoothByteParser,
            thermometerRepository = thermometerRepository,
            deviceRepository = deviceRepository,
            scope = testScope,
            onDeviceConnected = { deviceConnectedCallbackInvoked = true },
            onDeviceDisconnected = { deviceDisconnectedCallbackInvoked = true },
            onDeviceDataReceived = { deviceDataReceivedCallbackInvoked = true }
        )

        handler = BluetoothGattCallbackHandler(config)
    }

    @Test
    fun `onConnectionStateChange should invoke connected callback when state is connected`() {
        runTest {
            // When
            handler.onConnectionStateChange(
                bluetoothGatt,
                BluetoothGatt.GATT_SUCCESS,
                BluetoothProfile.STATE_CONNECTED
            )

            // Then
            deviceConnectedCallbackInvoked shouldBe true
            deviceDisconnectedCallbackInvoked shouldBe false
        }
    }

    @Test
    fun `onConnectionStateChange should invoke disconnected callback when state is disconnected`() {
        runTest {
            // When
            handler.onConnectionStateChange(
                bluetoothGatt,
                BluetoothGatt.GATT_SUCCESS,
                BluetoothProfile.STATE_DISCONNECTED
            )

            // Then
            deviceConnectedCallbackInvoked shouldBe false
            deviceDisconnectedCallbackInvoked shouldBe true
        }
    }

    @Test
    fun `onConnectionStateChange should handle connecting state gracefully`() {
        runTest {
            // When
            handler.onConnectionStateChange(
                bluetoothGatt,
                BluetoothGatt.GATT_SUCCESS,
                BluetoothProfile.STATE_CONNECTING
            )

            // Then - No callbacks should be invoked for connecting state
            deviceConnectedCallbackInvoked shouldBe false
            deviceDisconnectedCallbackInvoked shouldBe false
        }
    }

    @Test
    fun `onConnectionStateChange should handle disconnecting state gracefully`() {
        runTest {
            // When
            handler.onConnectionStateChange(
                bluetoothGatt,
                BluetoothGatt.GATT_SUCCESS,
                BluetoothProfile.STATE_DISCONNECTING
            )

            // Then - No callbacks should be invoked for disconnecting state
            deviceConnectedCallbackInvoked shouldBe false
            deviceDisconnectedCallbackInvoked shouldBe false
        }
    }

    @Test
    fun `onConnectionStateChange should handle failure status`() {
        runTest {
            // When - Connection failed
            handler.onConnectionStateChange(
                bluetoothGatt,
                BluetoothGatt.GATT_FAILURE,
                BluetoothProfile.STATE_DISCONNECTED
            )

            // Then - Should still invoke disconnected callback
            deviceDisconnectedCallbackInvoked shouldBe true
        }
    }

    @Test
    fun `resetTimestamp should reset read characteristic timestamp`() {
        runTest {
            // Given - Set up device status
            val status = DeviceConnectionStatus.connected(testDevice)
            every { deviceRepository.getDeviceStatus(testMacAddress) } returns status

            val testData = ThermometerValues.DataLYWSD03MMC(
                temperature = 22.5,
                humidity = 65,
                battery = 80.0,
                macAddress = testMacAddress,
                timestamp = java.time.LocalDateTime.now()
            )
            every { bluetoothByteParser.parseBytes(any(), any()) } returns testData

            // First characteristic change
            handler.onCharacteristicChanged(
                bluetoothGatt,
                bluetoothGattCharacteristic,
                byteArrayOf(0x01, 0x02, 0x03)
            )

            // When
            handler.resetTimestamp()

            // Then - Next characteristic change should be processed immediately
            handler.onCharacteristicChanged(
                bluetoothGatt,
                bluetoothGattCharacteristic,
                byteArrayOf(0x04, 0x05, 0x06)
            )

            // Both should have been processed
            coVerify(atLeast = 2) {
                thermometerRepository.saveCurrentMeasurement(any())
            }
        }
    }

    @Test
    fun `onCharacteristicChanged should not process when device not found`() {
        runTest {
            // Given
            every { deviceRepository.getDeviceStatus(testMacAddress) } returns null

            // When
            handler.onCharacteristicChanged(
                bluetoothGatt,
                bluetoothGattCharacteristic,
                byteArrayOf(0x01, 0x02, 0x03)
            )

            // Then
            coVerify(exactly = 0) { thermometerRepository.saveCurrentMeasurement(any()) }
            deviceDataReceivedCallbackInvoked shouldBe false
        }
    }

    @Test
    fun `onCharacteristicChanged should not save when parser returns null`() {
        runTest {
            // Given
            val status = DeviceConnectionStatus.connected(testDevice)
            every { deviceRepository.getDeviceStatus(testMacAddress) } returns status
            every { bluetoothByteParser.parseBytes(any(), any()) } returns "not a ThermometerValues"

            // When
            handler.onCharacteristicChanged(
                bluetoothGatt,
                bluetoothGattCharacteristic,
                byteArrayOf(0x01, 0x02, 0x03)
            )

            // Then
            coVerify(exactly = 0) { thermometerRepository.saveCurrentMeasurement(any()) }
        }
    }

    @Test
    fun `onCharacteristicChanged with ByteArray should parse and save data`() {
        runTest {
            // Given
            val status = DeviceConnectionStatus.connected(testDevice)
            every { deviceRepository.getDeviceStatus(testMacAddress) } returns status

            val testData = ThermometerValues.DataLYWSD03MMC(
                temperature = 22.5,
                humidity = 65,
                battery = 80.0,
                macAddress = testMacAddress,
                timestamp = java.time.LocalDateTime.now()
            )
            every { bluetoothByteParser.parseBytes(any(), any()) } returns testData

            // When
            handler.onCharacteristicChanged(
                bluetoothGatt,
                bluetoothGattCharacteristic,
                byteArrayOf(0x01, 0x02, 0x03, 0x04, 0x05)
            )

            // Then
            coVerify(exactly = 1) {
                thermometerRepository.saveCurrentMeasurement(
                    match { data ->
                        data is ThermometerValues.DataLYWSD03MMC &&
                            data.temperature == 22.5 &&
                            data.humidity == 65 &&
                            data.battery == 80.0 &&
                            data.macAddress == testMacAddress
                    }
                )
            }
            deviceDataReceivedCallbackInvoked shouldBe true
        }
    }

    @Test
    fun `onCharacteristicChanged should process data with valid device and parsed bytes`() {
        runTest {
            // Given
            val status = DeviceConnectionStatus.connected(testDevice)
            every { deviceRepository.getDeviceStatus(testMacAddress) } returns status

            val testData = ThermometerValues.DataLYWSD03MMC(
                temperature = -5.0,
                humidity = 30,
                battery = 100.0,
                macAddress = testMacAddress,
                timestamp = java.time.LocalDateTime.now()
            )
            every { bluetoothByteParser.parseBytes(any(), any()) } returns testData

            // When
            handler.onCharacteristicChanged(
                bluetoothGatt,
                bluetoothGattCharacteristic,
                byteArrayOf(0xFF.toByte(), 0xEE.toByte(), 0xDD.toByte())
            )

            // Then
            coVerify(exactly = 1) {
                thermometerRepository.saveCurrentMeasurement(
                    match { data ->
                        data is ThermometerValues.DataLYWSD03MMC &&
                            data.temperature == -5.0 &&
                            data.humidity == 30
                    }
                )
            }
            deviceDataReceivedCallbackInvoked shouldBe true
        }
    }

    @Test
    fun `onCharacteristicChanged should handle empty byte array`() {
        runTest {
            // Given
            val status = DeviceConnectionStatus.connected(testDevice)
            every { deviceRepository.getDeviceStatus(testMacAddress) } returns status
            every { bluetoothByteParser.parseBytes(any(), any()) } returns "not a ThermometerValues"

            // When
            handler.onCharacteristicChanged(
                bluetoothGatt,
                bluetoothGattCharacteristic,
                byteArrayOf()
            )

            // Then
            coVerify(exactly = 0) { thermometerRepository.saveCurrentMeasurement(any()) }
        }
    }

    @Test
    fun `multiple connection state changes should invoke callbacks correctly`() {
        runTest {
            // When - Connect, disconnect, connect again
            handler.onConnectionStateChange(
                bluetoothGatt,
                BluetoothGatt.GATT_SUCCESS,
                BluetoothProfile.STATE_CONNECTED
            )
            deviceConnectedCallbackInvoked shouldBe true
            deviceConnectedCallbackInvoked = false // Reset

            handler.onConnectionStateChange(
                bluetoothGatt,
                BluetoothGatt.GATT_SUCCESS,
                BluetoothProfile.STATE_DISCONNECTED
            )
            deviceDisconnectedCallbackInvoked shouldBe true
            deviceDisconnectedCallbackInvoked = false // Reset

            handler.onConnectionStateChange(
                bluetoothGatt,
                BluetoothGatt.GATT_SUCCESS,
                BluetoothProfile.STATE_CONNECTED
            )

            // Then
            deviceConnectedCallbackInvoked shouldBe true
        }
    }
}
