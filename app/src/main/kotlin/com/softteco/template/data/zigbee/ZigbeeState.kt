package com.softteco.template.data.zigbee

import com.softteco.template.utils.ZigbeeDevice

interface ZigbeeState {
    var deviceConnectedCallback: (() -> Unit)?
    var deviceDisconnectedCallback: (() -> Unit)?
    var scanResultCallback: ((device: ZigbeeDevice) -> Unit)?
    var deviceDataReceivedCallback: (() -> Unit)?
    var subscribedCallback: (() -> Unit)?
    var unsubscribedCallback: (() -> Unit)?
}
