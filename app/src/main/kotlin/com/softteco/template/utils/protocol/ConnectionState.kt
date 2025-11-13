package com.softteco.template.utils.protocol

/**
 * Represents the connection state of a device.
 */
enum class ConnectionState {
    /**
     * Device is idle, not performing any connection operations
     */
    IDLE,

    /**
     * Device is being searched for or scanning is in progress
     */
    SEARCHING,

    /**
     * Device is in the process of connecting
     */
    CONNECTING,

    /**
     * Device is successfully connected
     */
    CONNECTED,

    /**
     * Device is disconnected
     */
    DISCONNECTED
}
