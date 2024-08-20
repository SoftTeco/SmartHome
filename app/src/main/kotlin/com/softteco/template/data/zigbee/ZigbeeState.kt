package com.softteco.template.data.zigbee

import com.softteco.template.utils.ZigbeeDevice

interface ZigbeeState {
    var onConnect: (() -> Unit)?
    var onDisconnect: (() -> Unit)?
    var onScanResult: ((device: ZigbeeDevice) -> Unit)?
    var onDeviceResult: (() -> Unit)?
    var onSubscribed: (() -> Unit)?
    var onUnsubscribed: (() -> Unit)?
}
