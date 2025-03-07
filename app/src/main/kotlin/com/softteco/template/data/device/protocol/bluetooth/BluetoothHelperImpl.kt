package com.softteco.template.data.device.protocol.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothProfile
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import com.softteco.template.BuildConfig
import com.softteco.template.Constants.READ_BLUETOOTH_CHARACTERISTIC_DELAY
import com.softteco.template.data.base.error.Result
import com.softteco.template.data.bluetooth.BluetoothByteParser
import com.softteco.template.data.bluetooth.BluetoothHelper
import com.softteco.template.data.bluetooth.BluetoothState
import com.softteco.template.data.device.Device
import com.softteco.template.data.device.ProtocolType
import com.softteco.template.data.device.ThermometerData
import com.softteco.template.data.device.ThermometerRepository
import com.softteco.template.data.device.ThermometerValues
import com.softteco.template.data.device.protocol.common.DeviceOperationHandler
import com.softteco.template.utils.protocol.DeviceConnectionService
import com.softteco.template.utils.protocol.DeviceConnectionStatus
import com.softteco.template.utils.protocol.PermissionType
import com.softteco.template.utils.protocol.getBluetoothAdapter
import com.softteco.template.utils.protocol.isServiceRunning
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat
import no.nordicsemi.android.support.v18.scanner.ScanCallback
import no.nordicsemi.android.support.v18.scanner.ScanResult
import timber.log.Timber
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@SuppressLint("MissingPermission")
@Singleton
internal class BluetoothHelperImpl @Inject constructor(
    private val bluetoothByteParser: BluetoothByteParser,
    private val thermometerRepository: ThermometerRepository
) : BluetoothHelper, BluetoothState {

    private var deviceOperationHandler: DeviceOperationHandler? = null
    private lateinit var bluetoothReceiver: BroadcastReceiver
    private var resultLocationEnableLauncher: ActivityResultLauncher<Intent>? = null
    private var savedBluetoothDevices = mutableListOf<Device>()
    override var onConnect: (() -> Unit)? = null
    override var onDisconnect: (() -> Unit)? = null
    override var onScanResult: ((scanResult: ScanResult) -> Unit)? = null
    override var onDeviceResult: (() -> Unit)? = null
    override var onBluetoothModuleChangeState: ((ifTurnOn: Boolean) -> Unit)? = null
    private var readCharacteristicTimestamp = 0L

    private val _deviceConnectionStatusList =
        MutableStateFlow<Map<String, DeviceConnectionStatus>>(emptyMap())
    private val deviceConnectionStatusList: StateFlow<Map<String, DeviceConnectionStatus>> =
        _deviceConnectionStatusList

    private var connectedDevicesList = hashMapOf<String, BluetoothGatt>()

    private val scanCallback: ScanCallback = object : ScanCallback() {
        override fun onScanResult(
            callbackType: Int,
            scanResult: ScanResult
        ) {
            super.onScanResult(callbackType, scanResult)
            scanResult.device.name?.let {
                _deviceConnectionStatusList.update { currentMap ->
                    currentMap.toMutableMap().apply {
                        this[scanResult.device.address] = DeviceConnectionStatus(
                            Device.Basic(
                                type = Device.Type.TemperatureAndHumidity,
                                family = Device.Family.Sensor,
                                model = deviceOperationHandler?.getDeviceModel(it)
                                    ?: Device.Model.Unknown,
                                id = UUID.randomUUID(),
                                defaultName = it,
                                name = "Temperature and Humidity Monitor",
                                macAddress = scanResult.device.address,
                                img = deviceOperationHandler?.getDeviceImage(it),
                                location = "",
                                protocolType = ProtocolType.BLUETOOTH
                            ),
                            false
                        )
                    }
                }
                onScanResult?.invoke(scanResult)
            }
        }
    }

    override fun init(deviceOperationHandler: DeviceOperationHandler) {
        this.deviceOperationHandler = deviceOperationHandler
        bluetoothReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                when (
                    intent.getIntExtra(
                        BluetoothAdapter.EXTRA_STATE,
                        BluetoothAdapter.STATE_OFF
                    )
                ) {
                    BluetoothAdapter.STATE_ON -> {
                        startScan()
                        onBluetoothModuleChangeState?.invoke(true)
                    }

                    BluetoothAdapter.STATE_OFF -> {
                        stopService()
                        stopScan()
                        onBluetoothModuleChangeState?.invoke(false)
                    }
                }
            }
        }
        runBlocking {
            withContext(Dispatchers.IO) {
                when (val result = thermometerRepository.getDevices()) {
                    is Result.Success -> {
                        result.data.filter { it.protocolType == ProtocolType.BLUETOOTH }.let {
                            savedBluetoothDevices.addAll(it)
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

    override fun shutdown() {
        stopService()
        unregisterReceiver()
    }

    override fun provideConnectionToTheDevice(bluetoothDevice: BluetoothDevice) {
        if (checkConnectedDevice(bluetoothDevice.address)) {
            disconnect(bluetoothDevice.address)
        } else {
            bluetoothDevice.connectGatt(
                deviceOperationHandler?.getContext(),
                false,
                mGattCallback,
                BluetoothDevice.TRANSPORT_LE
            )
        }
    }

    override fun connect(macAddress: String) {
        deviceOperationHandler?.getContext()?.getBluetoothAdapter()?.getRemoteDevice(macAddress)
            ?.let {
                CoroutineScope(Dispatchers.IO).launch {
                    provideConnectionToTheDevice(it)
                }
            }
    }

    override fun registerReceiver() {
        deviceOperationHandler?.registerReceiver(
            bluetoothReceiver,
            IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        )
    }

    @Suppress("TooGenericExceptionCaught")
    override fun unregisterReceiver() {
        try {
            deviceOperationHandler?.unregisterReceiver(bluetoothReceiver)
        } catch (e: Exception) {
            Timber.e("Error unregister receiver", e)
        }
    }

    override fun checkConnectedDevice(macAddress: String): Boolean {
        val statusMap = runBlocking { deviceConnectionStatusList.first() }
        return statusMap[macAddress]?.isConnected ?: false
    }

    private fun stopScan() {
        BluetoothLeScannerCompat.getScanner().stopScan(scanCallback)
    }

    private val mGattCallback: BluetoothGattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                provideConnectedState(gatt)
            }
            if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                gatt.let {
                    _deviceConnectionStatusList.update { currentMap ->
                        currentMap.toMutableMap().apply {
                            this[it.device.address]?.let { status ->
                                val updatedStatus = status.copy(isConnected = false)
                                this[it.device.address] = updatedStatus
                            }
                        }
                    }
                    it.close()
                    onDisconnect?.invoke()
                    stopService()
                }
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            super.onServicesDiscovered(gatt, status)
            if (status == BluetoothGatt.GATT_SUCCESS) {
                gatt?.let { gatt ->
                    gatt.getService(UUID.fromString(BuildConfig.BLUETOOTH_SERVICE_UUID_VALUE))
                        .getCharacteristic(UUID.fromString(BuildConfig.BLUETOOTH_CHARACTERISTIC_UUID_VALUE))
                        .let { characteristic ->
                            setCharacteristicNotification(gatt, characteristic, true)
                        }
                }
            }
        }

        fun setCharacteristicNotification(
            bluetoothGatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            enable: Boolean
        ): Boolean {
            bluetoothGatt.setCharacteristicNotification(characteristic, enable)
            val descriptor =
                characteristic.getDescriptor(UUID.fromString(BuildConfig.BLUETOOTH_DESCRIPTOR_UUID_VALUE))
            descriptor.value =
                if (enable) {
                    BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                } else {
                    byteArrayOf(
                        0x00,
                        0x00
                    )
                }
            return bluetoothGatt.writeDescriptor(descriptor)
        }

        @Deprecated("Deprecated in Java")
        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
            characteristic.value.let { characteristic ->
                if (System.currentTimeMillis() - readCharacteristicTimestamp >= READ_BLUETOOTH_CHARACTERISTIC_DELAY) {
                    readCharacteristicTimestamp = System.currentTimeMillis()
                    _deviceConnectionStatusList.value[gatt.device.address]?.device?.let { device ->
                        val bluetoothDeviceData = bluetoothByteParser.parseBytes(
                            characteristic,
                            device.model
                        ) as ThermometerValues.DataLYWSD03MMC
                        onDeviceResult?.invoke()
                        runBlocking {
                            withContext(Dispatchers.IO) {
                                thermometerRepository.saveCurrentMeasurement(
                                    ThermometerValues.DataLYWSD03MMC(
                                        bluetoothDeviceData.temperature,
                                        bluetoothDeviceData.humidity,
                                        bluetoothDeviceData.battery,
                                        device.macAddress,
                                        LocalDateTime.now(),
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    override fun disconnect(macAddress: String) {
        val bluetoothGatt = connectedDevicesList[macAddress]
        bluetoothGatt?.disconnect()
        readCharacteristicTimestamp = 0L
    }

    override fun startScan() {
        deviceOperationHandler?.let {
            if (it.checkBluetoothSupport() && it.hasPermissions()) {
                when (it.checkEnableDeviceModules()) {
                    PermissionType.LOCATION_TURNED_OFF -> {
                        it.startIntent(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                    }

                    PermissionType.BLUETOOTH_TURNED_OFF -> {
                        it.startIntent(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
                    }

                    PermissionType.BLUETOOTH_AND_LOCATION_TURNED_ON -> {
                        stopScan()
                        BluetoothLeScannerCompat.getScanner().startScan(scanCallback)
                    }
                }
            }
        }
    }

    override fun onScanCallback(onScanResult: (scanResult: ScanResult) -> Unit) {
        this.onScanResult = onScanResult
    }

    override fun onConnectCallback(onConnect: () -> Unit) {
        this.onConnect = onConnect
    }

    override fun onDisconnectCallback(onDisconnect: () -> Unit) {
        this.onDisconnect = onDisconnect
    }

    override fun onDeviceResultCallback(onDeviceResult: () -> Unit) {
        this.onDeviceResult = onDeviceResult
    }

    override fun onBluetoothModuleChangeStateCallback(onBluetoothModuleChangeState: (ifTurnOn: Boolean) -> Unit) {
        this.onBluetoothModuleChangeState = onBluetoothModuleChangeState
    }

    override fun getObservableDeviceConnectionStatusList() = deviceConnectionStatusList

    private fun provideConnectedState(bluetoothGatt: BluetoothGatt) {
        bluetoothGatt.let {
            connectedDevicesList[it.device.address] = it
            _deviceConnectionStatusList.update { currentMap ->
                currentMap.toMutableMap().apply {
                    this[it.device.address]?.let { status ->
                        val updatedStatus = status.copy(isConnected = true)
                        this[it.device.address] = updatedStatus
                        if (savedBluetoothDevices.none { o -> o.macAddress == it.device.address }) {
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
            it.discoverServices()
            onConnect?.invoke()
            if (deviceOperationHandler?.getContext()?.isServiceRunning(DeviceConnectionService::class.java) == false) {
                deviceOperationHandler?.startConnectionService(DeviceConnectionService::class.java)
            }
        }
    }

    private fun stopService() {
//        if (!checkRemainingConnectionForService(
//                getObservableDeviceConnectionStatusList(),
//                activity?.zigbeeHelper?.getObservableDeviceConnectionStatusList()
//            )
//        ) {
            deviceOperationHandler?.stopConnectionService(DeviceConnectionService::class.java)
//        }
    }
}
