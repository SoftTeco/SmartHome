package com.softteco.template.data.bluetooth

import com.softteco.template.data.bluetooth.entity.BluetoothDeviceData
import no.nordicsemi.android.support.v18.scanner.ScanResult

interface BluetoothState {
    var onConnect: (() -> Unit)?
    var onDisconnect: (() -> Unit)?
    var onScanResult: ((scanResult: ScanResult) -> Unit)?
    var onDeviceResult: ((bluetoothDeviceData: BluetoothDeviceData) -> Unit)?
    var onBluetoothModuleChangeState: ((ifTurnOn: Boolean) -> Unit)?
}
