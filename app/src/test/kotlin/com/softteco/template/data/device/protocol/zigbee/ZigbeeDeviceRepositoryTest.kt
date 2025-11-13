package com.softteco.template.data.device.protocol.zigbee

import app.cash.turbine.test
import com.softteco.template.BaseTest
import com.softteco.template.data.base.error.Result
import com.softteco.template.data.device.Device
import com.softteco.template.data.device.ProtocolType
import com.softteco.template.data.device.ThermometerRepository
import com.softteco.template.utils.MainDispatcherExtension
import com.softteco.template.utils.protocol.DeviceConnectionStatus
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
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
class ZigbeeDeviceRepositoryTest : BaseTest() {

    @RelaxedMockK
    private lateinit var thermometerRepository: ThermometerRepository

    private lateinit var repository: ZigbeeDeviceRepository
    private lateinit var testScope: TestScope

    private val testDevice = Device.Basic(
        id = java.util.UUID.randomUUID(),
        defaultName = "Zigbee Device",
        name = "Zigbee Device",
        macAddress = "AA:BB:CC:DD:EE:FF",
        img = null,
        location = "",
        type = Device.Type.TemperatureAndHumidity,
        family = Device.Family.Sensor,
        model = Device.Model.LYWSD03MMC,
        protocolType = ProtocolType.ZIGBEE
    )

    private val testDevice2 = Device.Basic(
        id = java.util.UUID.randomUUID(),
        defaultName = "Zigbee Device 2",
        name = "Zigbee Device 2",
        macAddress = "11:22:33:44:55:66",
        img = null,
        location = "",
        type = Device.Type.TemperatureAndHumidity,
        family = Device.Family.Sensor,
        model = Device.Model.LYWSD03MMC,
        protocolType = ProtocolType.ZIGBEE
    )

    @BeforeEach
    fun setup() {
        testScope = TestScope(UnconfinedTestDispatcher())
        repository = ZigbeeDeviceRepository(thermometerRepository, testScope)
    }

    @Test
    fun `loadSavedDevices should load zigbee devices and update status list`() {
        runTest {
            // Given
            val devices = listOf(testDevice, testDevice2)
            coEvery { thermometerRepository.getDevices() } returns Result.Success(devices)

            // When
            repository.loadSavedDevices()

            // Then
            repository.deviceConnectionStatusList.test {
                val statusMap = awaitItem()
                statusMap.size shouldBe 2
                statusMap[testDevice.macAddress]?.isConnected shouldBe false
                statusMap[testDevice2.macAddress]?.isConnected shouldBe false
            }

            coVerify(exactly = 1) { thermometerRepository.getDevices() }
        }
    }

    @Test
    fun `loadSavedDevices should filter non-zigbee devices`() {
        runTest {
            // Given
            val bluetoothDevice = Device.Basic(
                id = java.util.UUID.randomUUID(),
                defaultName = "Bluetooth Device",
                name = "Bluetooth Device",
                macAddress = "FF:EE:DD:CC:BB:AA",
                img = null,
                location = "",
                type = Device.Type.TemperatureAndHumidity,
                family = Device.Family.Sensor,
                model = Device.Model.LYWSD03MMC,
                protocolType = ProtocolType.BLUETOOTH
            )
            val devices = listOf(testDevice, bluetoothDevice)
            coEvery { thermometerRepository.getDevices() } returns Result.Success(devices)

            // When
            repository.loadSavedDevices()

            // Then
            repository.deviceConnectionStatusList.test {
                val statusMap = awaitItem()
                statusMap.size shouldBe 1
                statusMap.containsKey(testDevice.macAddress) shouldBe true
                statusMap.containsKey(bluetoothDevice.macAddress) shouldBe false
            }
        }
    }

    @Test
    fun `loadSavedDevices should handle error result gracefully`() {
        runTest {
            // Given
            coEvery { thermometerRepository.getDevices() } returns Result.Error(
                error = com.softteco.template.data.base.error.AppError.UnknownError()
            )

            // When
            repository.loadSavedDevices()

            // Then
            repository.deviceConnectionStatusList.test {
                val statusMap = awaitItem()
                statusMap.size shouldBe 0
            }
        }
    }

    @Test
    fun `updateDeviceStatus should update device status in flow`() {
        runTest {
            // Given
            val status = DeviceConnectionStatus.connected(testDevice)

            // When & Then
            repository.deviceConnectionStatusList.test {
                awaitItem() // Initial empty state

                repository.updateDeviceStatus(testDevice.macAddress, status)

                val statusMap = awaitItem()
                statusMap[testDevice.macAddress]?.isConnected shouldBe true
                statusMap[testDevice.macAddress]?.device shouldBe testDevice
            }
        }
    }

    @Test
    fun `getDeviceStatus should return correct status`() {
        runTest {
            // Given
            val status = DeviceConnectionStatus.connecting(testDevice)
            repository.updateDeviceStatus(testDevice.macAddress, status)

            // When
            val result = repository.getDeviceStatus(testDevice.macAddress)

            // Then
            result?.connectionState shouldBe com.softteco.template.utils.protocol.ConnectionState.CONNECTING
            result?.device shouldBe testDevice
        }
    }

    @Test
    fun `getDeviceStatus should return null for unknown device`() {
        runTest {
            // When
            val result = repository.getDeviceStatus("unknown:mac:address")

            // Then
            result shouldBe null
        }
    }

    @Test
    fun `isDeviceSaved should return false for unsaved device`() {
        runTest {
            // When
            val result = repository.isDeviceSaved("unknown:mac:address")

            // Then
            result shouldBe false
        }
    }

    @Test
    fun `saveNewDevice should save device and thermometer data`() {
        runTest {
            // When
            repository.saveNewDevice(testDevice)

            // Then
            coVerify(exactly = 1) { thermometerRepository.saveDevice(testDevice) }
            coVerify(exactly = 1) {
                thermometerRepository.saveThermometerData(
                    match { data ->
                        data.deviceId == testDevice.id &&
                            data.deviceName == testDevice.name &&
                            data.macAddress == testDevice.macAddress
                    }
                )
            }
        }
    }

    @Test
    fun `isConnected should return true for connected device`() {
        runTest {
            // Given
            val status = DeviceConnectionStatus.connected(testDevice)
            repository.updateDeviceStatus(testDevice.macAddress, status)

            // When
            val result = repository.isConnected(testDevice.macAddress)

            // Then
            result shouldBe true
        }
    }

    @Test
    fun `isConnected should return false for disconnected device`() {
        runTest {
            // Given
            val status = DeviceConnectionStatus.disconnected(testDevice)
            repository.updateDeviceStatus(testDevice.macAddress, status)

            // When
            val result = repository.isConnected(testDevice.macAddress)

            // Then
            result shouldBe false
        }
    }

    @Test
    fun `isConnected should return false for unknown device`() {
        runTest {
            // When
            val result = repository.isConnected("unknown:mac:address")

            // Then
            result shouldBe false
        }
    }

    @Test
    fun `removeDevice should remove device from cache and status list`() {
        runTest {
            // Given
            val devices = listOf(testDevice)
            coEvery { thermometerRepository.getDevices() } returns Result.Success(devices)
            repository.loadSavedDevices()
            val status = DeviceConnectionStatus.connected(testDevice)
            repository.updateDeviceStatus(testDevice.macAddress, status)

            // When & Then
            repository.deviceConnectionStatusList.test {
                skipItems(1) // Skip initial load

                repository.removeDevice(testDevice.macAddress)

                val statusMap = awaitItem()
                statusMap.containsKey(testDevice.macAddress) shouldBe false
                repository.isDeviceSaved(testDevice.macAddress) shouldBe false
            }
        }
    }

    @Test
    fun `updateDeviceStatus should maintain multiple device statuses`() {
        runTest {
            // Given
            val status1 = DeviceConnectionStatus.connected(testDevice)
            val status2 = DeviceConnectionStatus.connecting(testDevice2)

            // When & Then
            repository.deviceConnectionStatusList.test {
                awaitItem() // Initial empty state

                repository.updateDeviceStatus(testDevice.macAddress, status1)
                awaitItem()

                repository.updateDeviceStatus(testDevice2.macAddress, status2)

                val statusMap = awaitItem()
                statusMap.size shouldBe 2
                statusMap[testDevice.macAddress]?.isConnected shouldBe true
                statusMap[testDevice2.macAddress]?.connectionState shouldBe
                    com.softteco.template.utils.protocol.ConnectionState.CONNECTING
            }
        }
    }

    @Test
    fun `updateDeviceStatus should replace existing status for same device`() {
        runTest {
            // Given
            val connectingStatus = DeviceConnectionStatus.connecting(testDevice)
            val connectedStatus = DeviceConnectionStatus.connected(testDevice)

            // When & Then
            repository.deviceConnectionStatusList.test {
                awaitItem() // Initial empty state

                repository.updateDeviceStatus(testDevice.macAddress, connectingStatus)
                val firstUpdate = awaitItem()
                firstUpdate[testDevice.macAddress]?.connectionState shouldBe
                    com.softteco.template.utils.protocol.ConnectionState.CONNECTING

                repository.updateDeviceStatus(testDevice.macAddress, connectedStatus)
                val secondUpdate = awaitItem()
                secondUpdate[testDevice.macAddress]?.isConnected shouldBe true
                secondUpdate[testDevice.macAddress]?.connectionState shouldBe
                    com.softteco.template.utils.protocol.ConnectionState.CONNECTED
            }
        }
    }
}
