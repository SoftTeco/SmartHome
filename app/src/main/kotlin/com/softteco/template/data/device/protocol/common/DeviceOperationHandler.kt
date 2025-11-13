package com.softteco.template.data.device.protocol.common

import android.app.Service
import com.softteco.template.data.device.Device

/**
 * Interface for device-specific operations.
 * This interface handles device information and service management,
 * without direct dependencies on Activity.
 */
interface DeviceOperationHandler {
    /**
     * Get the device model based on the device name.
     * @param deviceName The name of the device
     * @return The device model
     */
    fun getDeviceModel(deviceName: String): Device.Model
    
    /**
     * Get the device image resource path based on the device name.
     * @param deviceName The name of the device
     * @return The image resource path
     */
    fun getDeviceImage(deviceName: String): String
    
    /**
     * Start a connection service for maintaining device connections.
     * @param serviceClass The service class to start
     */
    fun startConnectionService(serviceClass: Class<out Service>)
    
    /**
     * Stop a connection service.
     * @param serviceClass The service class to stop
     */
    fun stopConnectionService(serviceClass: Class<out Service>)
}
