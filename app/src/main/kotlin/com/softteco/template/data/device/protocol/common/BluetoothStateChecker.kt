package com.softteco.template.data.device.protocol.common

import com.softteco.template.utils.protocol.PermissionType

/**
 * Interface for checking Bluetooth and location module states.
 * Separates hardware state checking from business logic.
 */
interface BluetoothStateChecker {
    /**
     * Check if the device supports Bluetooth LE.
     * @return true if Bluetooth LE is supported, false otherwise
     */
    fun isBluetoothSupported(): Boolean

    /**
     * Check the state of device modules (Bluetooth and Location).
     * @return PermissionType indicating which modules need to be enabled
     */
    fun checkModulesState(): PermissionType
}
