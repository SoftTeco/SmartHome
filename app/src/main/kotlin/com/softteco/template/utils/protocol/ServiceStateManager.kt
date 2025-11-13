package com.softteco.template.utils.protocol

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Centralized manager for tracking service states using StateFlow.
 * Provides a reactive way to observe service lifecycle without deprecated APIs.
 *
 * This is a modern replacement for the deprecated ActivityManager.getRunningServices() API.
 * Services notify this manager about their lifecycle events (onCreate/onDestroy),
 * and consumers can either check the current state or observe state changes reactively.
 *
 * Benefits:
 * - No deprecated APIs
 * - Reactive state observation via Flow
 * - Thread-safe state management
 * - Centralized service state tracking
 * - Easy to extend for multiple services
 */
object ServiceStateManager {

    private val _deviceConnectionServiceState = MutableStateFlow(ServiceState.STOPPED)

    /**
     * Observable state flow for device connection service.
     * Consumers can collect this flow to reactively observe service state changes.
     */
    val deviceConnectionServiceState: StateFlow<ServiceState> = _deviceConnectionServiceState.asStateFlow()

    /**
     * Notify that device connection service has started.
     * Should be called from service's onCreate() method.
     */
    internal fun notifyDeviceConnectionServiceStarted() {
        _deviceConnectionServiceState.value = ServiceState.RUNNING
    }

    /**
     * Notify that device connection service has stopped.
     * Should be called from service's onDestroy() method.
     */
    internal fun notifyDeviceConnectionServiceStopped() {
        _deviceConnectionServiceState.value = ServiceState.STOPPED
    }

    /**
     * Check if device connection service is currently running.
     * @return true if the service is running, false otherwise
     */
    fun isDeviceConnectionServiceRunning(): Boolean {
        return _deviceConnectionServiceState.value == ServiceState.RUNNING
    }
}

/**
 * Possible states for a service.
 */
enum class ServiceState {
    /** Service is currently running (between onCreate and onDestroy) */
    RUNNING,

    /** Service is not running */
    STOPPED
}
