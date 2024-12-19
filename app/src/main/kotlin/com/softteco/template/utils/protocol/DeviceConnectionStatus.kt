package com.softteco.template.utils.protocol

import com.softteco.template.data.device.Device

data class DeviceConnectionStatus(
    val device: Device,
    var isConnected: Boolean
)
