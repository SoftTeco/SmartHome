package com.softteco.template.data.device.protocol.common

import android.content.Intent

/**
 * Interface for launching system intents (e.g., location settings, Bluetooth settings).
 * Separates Activity-specific intent launching from business logic.
 */
interface IntentLauncher {
    /**
     * Launch a system intent (e.g., to enable location or Bluetooth).
     * @param intent The intent to launch
     */
    fun launchIntent(intent: Intent)
}

