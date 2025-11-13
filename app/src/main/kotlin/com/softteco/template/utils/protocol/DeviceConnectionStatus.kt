package com.softteco.template.utils.protocol

import com.softteco.template.data.device.Device

data class DeviceConnectionStatus(
    val device: Device,
    var isConnected: Boolean,
    var connectionState: ConnectionState = ConnectionState.IDLE
) {
    companion object {
        fun idle(device: Device) = DeviceConnectionStatus(
            device = device,
            isConnected = false,
            connectionState = ConnectionState.IDLE
        )

        fun searching(device: Device) = DeviceConnectionStatus(
            device = device,
            isConnected = false,
            connectionState = ConnectionState.SEARCHING
        )

        fun connecting(device: Device) = DeviceConnectionStatus(
            device = device,
            isConnected = false,
            connectionState = ConnectionState.CONNECTING
        )

        fun connected(device: Device) = DeviceConnectionStatus(
            device = device,
            isConnected = true,
            connectionState = ConnectionState.CONNECTED
        )

        fun disconnected(device: Device) = DeviceConnectionStatus(
            device = device,
            isConnected = false,
            connectionState = ConnectionState.DISCONNECTED
        )
    }
}
