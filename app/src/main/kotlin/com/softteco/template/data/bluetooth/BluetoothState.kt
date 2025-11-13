package com.softteco.template.data.bluetooth

import no.nordicsemi.android.support.v18.scanner.ScanResult

interface BluetoothState {
    var deviceConnectedCallback: (() -> Unit)?
    var deviceDisconnectedCallback: (() -> Unit)?
    var scanResultCallback: ((scanResult: ScanResult) -> Unit)?
    var deviceDataReceivedCallback: (() -> Unit)?
    var bluetoothStateChangedCallback: ((isEnabled: Boolean) -> Unit)?
}
