package com.softteco.template.data.device.protocol.common

/**
 * Interface for handling Bluetooth-related permissions.
 * Separates permission logic from Activity dependencies.
 */
interface PermissionHandler {
    /**
     * Check if all necessary Bluetooth permissions are granted.
     * If not granted, this method should request them.
     * @return true if all permissions are granted, false otherwise
     */
    fun hasBluetoothPermissions(): Boolean
}

