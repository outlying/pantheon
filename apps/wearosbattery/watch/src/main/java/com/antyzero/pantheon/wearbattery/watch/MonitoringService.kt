package com.antyzero.pantheon.wearbattery.watch

import android.app.ActivityManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.IBinder
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.antyzero.pantheon.common.android.isServiceRunning


class MonitoringService : Service() {
    private var batteryLevelReceiver: BroadcastReceiver? = null
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        val notification: Notification = Notification.Builder(this, CHANNEL_ID)
            .setContentTitle("Battery Monitoring")
            .setContentText("Monitoring battery status...")
            .setSmallIcon(android.R.drawable.sym_def_app_icon)
            .build()

        startForeground(1, notification)

        batteryLevelReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                val batteryPct = level / scale.toFloat()
                Toast.makeText(
                    context,
                    "Battery Level: " + (batteryPct * 100).toInt() + "%",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        this.registerReceiver(batteryLevelReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
    }

    private fun createNotificationChannel() {
        val name: CharSequence = getString(R.string.channel_name)
        val description: String = getString(R.string.channel_description)
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(CHANNEL_ID, name, importance)
        channel.description = description
        val notificationManager: NotificationManager = getSystemService(
            NotificationManager::class.java
        )
        notificationManager.createNotificationChannel(channel)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        unregisterReceiver(batteryLevelReceiver)
        super.onDestroy()
    }

    companion object {
        private const val CHANNEL_ID = "BatteryServiceChannel"

        fun start(context: Context) {
            if(context.isServiceRunning(MonitoringService::class.java)) {
                return
            }
            val serviceIntent = Intent(context, MonitoringService::class.java)
            ContextCompat.startForegroundService(context, serviceIntent)
        }
    }
}