package com.softteco.template.utils.bluetooth

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.softteco.template.MainActivity
import com.softteco.template.R
import timber.log.Timber

class BluetoothDeviceConnectionService : Service() {

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val BLUETOOTH_DEVICE_SERVICE_NOTIFICATION_ID = 1
    }

    @Suppress("TooGenericExceptionCaught")
    @SuppressLint("ForegroundServiceType")
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        createNotificationChannel()
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE
        )
        val notification: Notification = NotificationCompat.Builder(
            this,
            this.getString(R.string.bluetooth_notification_channel_id)
        )
            .setContentTitle(getString(R.string.bluetooth_connection_is_running))
            .setContentText(getString(R.string.bluetooth_connection_active))
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .build()

        try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                startForeground(BLUETOOTH_DEVICE_SERVICE_NOTIFICATION_ID, notification)
            } else {
                startForeground(
                    BLUETOOTH_DEVICE_SERVICE_NOTIFICATION_ID,
                    notification,
                    FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE
                )
            }
        } catch (e: Exception) {
            Timber.e("Error starting service", e)
        }

        return START_NOT_STICKY
    }

    private fun createNotificationChannel() {
        val serviceChannel = NotificationChannel(
            getString(R.string.bluetooth_notification_channel_id),
            getString(R.string.bluetooth_connection),
            NotificationManager.IMPORTANCE_DEFAULT
        )
        val manager = getSystemService(
            NotificationManager::class.java
        )
        manager.createNotificationChannel(serviceChannel)
    }
}
