package com.softteco.template.data.device.protocol.common

import android.content.BroadcastReceiver
import android.content.IntentFilter

/**
 * Interface for managing BroadcastReceiver registration and unregistration.
 * Separates receiver management from Activity dependencies.
 */
interface ReceiverManager {
    /**
     * Register a BroadcastReceiver with the specified intent filter.
     * @param receiver The receiver to register
     * @param filter The intent filter
     */
    fun registerReceiver(receiver: BroadcastReceiver, filter: IntentFilter)

    /**
     * Unregister a previously registered BroadcastReceiver.
     * @param receiver The receiver to unregister
     */
    fun unregisterReceiver(receiver: BroadcastReceiver)
}
