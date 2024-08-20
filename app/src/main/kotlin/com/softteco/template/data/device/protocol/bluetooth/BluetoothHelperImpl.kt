package com.softteco.template.data.device.protocol.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.LocationManager
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.softteco.template.BuildConfig
import com.softteco.template.Constants.READ_BLUETOOTH_CHARACTERISTIC_DELAY
import com.softteco.template.MainActivity
import com.softteco.template.data.base.error.Result
import com.softteco.template.data.bluetooth.BluetoothByteParser
import com.softteco.template.data.bluetooth.BluetoothHelper
import com.softteco.template.data.bluetooth.BluetoothPermissionChecker
import com.softteco.template.data.bluetooth.BluetoothState
import com.softteco.template.data.device.Device
import com.softteco.template.data.device.ProtocolType
import com.softteco.template.data.device.ThermometerData
import com.softteco.template.data.device.ThermometerRepository
import com.softteco.template.data.device.ThermometerValues
import com.softteco.template.utils.protocol.DeviceConnectionService
import com.softteco.template.utils.protocol.DeviceConnectionStatus
import com.softteco.template.utils.protocol.checkRemainingConnectionForService
import com.softteco.template.utils.protocol.getDeviceImage
import com.softteco.template.utils.protocol.getDeviceModel
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
    private val bluetoothPermissionChecker: BluetoothPermissionChecker,
    private val bluetoothByteParser: BluetoothByteParser,
    private val thermometerRepository: ThermometerRepository
) : BluetoothHelper, BluetoothState {

    private var activity: MainActivity? = null
    private lateinit var bluetoothReceiver: BroadcastReceiver
    private lateinit var bluetoothManager: BluetoothManager
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var locationManager: LocationManager
    private var resultBluetoothEnableLauncher: ActivityResultLauncher<Intent>? = null
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
                                model = activity?.getDeviceModel(it) ?: Device.Model.Unknown,
                                id = UUID.randomUUID(),
                                defaultName = it,
                                name = "Temperature and Humidity Monitor",
                                macAddress = scanResult.device.address,
                                img = activity?.getDeviceImage(it),
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

    override fun init(activity: MainActivity) {
        this.activity = activity
        resultBluetoothEnableLauncher =
            this.activity?.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {}
        resultLocationEnableLauncher =
            this.activity?.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {}
        bluetoothReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                when (
                    intent.getIntExtra(
                        BluetoothAdapter.EXTRA_STATE,
                        BluetoothAdapter.STATE_OFF
                    )
                ) {
                    BluetoothAdapter.STATE_ON -> {
                        startScanIfHasPermissions()
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
        bluetoothManager =
            this.activity?.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter
        locationManager =
            this.activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
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

    override fun drop() {
        stopService()
        unregisterReceiver()
        this.activity = null
    }

    override suspend fun provideConnectionToDevice(bluetoothDevice: BluetoothDevice) {
        if (checkConnectedDevice(bluetoothDevice)) {
            disconnectFromDevice(connectedDevicesList[bluetoothDevice.address])
        } else {
            bluetoothDevice.connectGatt(
                activity?.applicationContext,
                false,
                mGattCallback,
                BluetoothDevice.TRANSPORT_LE
            )
        }
    }

    override fun provideConnectionToDeviceViaMacAddress(macAddress: String) {
        bluetoothAdapter.getRemoteDevice(macAddress)?.let {
            CoroutineScope(Dispatchers.IO).launch {
                provideConnectionToDevice(it)
            }
        }
    }

    override fun registerReceiver() {
        activity?.registerReceiver(
            bluetoothReceiver,
            IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        )
    }

    override fun unregisterReceiver() {
        try {
            activity?.unregisterReceiver(bluetoothReceiver)
        } catch (e: Exception) {
            Timber.e("Error unregister receiver", e)
        }
    }

    private fun stopScan() {
        BluetoothLeScannerCompat.getScanner().stopScan(scanCallback)
    }

    private suspend fun checkConnectedDevice(bluetoothDevice: BluetoothDevice): Boolean {
        val statusMap = deviceConnectionStatusList.first()
        return statusMap[bluetoothDevice.address]?.isConnected ?: false
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

    override fun disconnectFromDevice(bluetoothGatt: BluetoothGatt?) {
        bluetoothGatt?.disconnect()
        readCharacteristicTimestamp = 0L
    }

    override fun startScanIfHasPermissions() {
        activity?.let {
            if (bluetoothPermissionChecker.checkBluetoothSupport(bluetoothAdapter, it) &&
                bluetoothPermissionChecker.hasPermissions(it)
            ) {
                when (
                    bluetoothPermissionChecker.checkEnableDeviceModules(
                        bluetoothAdapter,
                        locationManager
                    )
                ) {
                    PermissionType.LOCATION_TURNED_OFF -> {
                        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                        resultLocationEnableLauncher?.launch(intent)
                    }

                    PermissionType.BLUETOOTH_TURNED_OFF -> {
                        val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                        resultBluetoothEnableLauncher?.launch(intent)
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

    private fun stopService() {
        if (!checkRemainingConnectionForService(
                getObservableDeviceConnectionStatusList(),
                activity?.zigbeeHelper?.getObservableDeviceConnectionStatusList()
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
