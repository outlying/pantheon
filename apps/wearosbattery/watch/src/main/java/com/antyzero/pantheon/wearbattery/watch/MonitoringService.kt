package com.antyzero.pantheon.wearbattery.watch

import android.Manifest
import android.app.ActivityManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.BatteryManager
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import com.antyzero.pantheon.common.android.isServiceRunning
import kotlin.random.Random


class MonitoringService : Service() {

    private var batteryLevelReceiver: BroadcastReceiver? = null
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()

        val notificationBuilder = Notification.Builder(this, CHANNEL_ID)
            .setContentText("Monitoring watch status...")
            .setSmallIcon(android.R.drawable.sym_def_app_icon)

        startForeground(1, notificationBuilder.build())

        batteryLevelReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                val batteryPct = level / scale.toFloat()

                val notificationManager = NotificationManagerCompat.from(this@MonitoringService)
                val notification = notificationBuilder
                    // .setContentText("$batteryPct")
                    .setContentText(Random.nextInt(100).toString())
                    .build()

                if (ActivityCompat.checkSelfPermission(
                        this@MonitoringService,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return
                }
                notificationManager.notify(1, notification)
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
        private const val CHANNEL_ID = "MonitoringServiceChannel"

        enum class Result {
            RUNNING, STARTED, MISSING_NOTIFICATION_PERMISSION
        }

        fun start(context: Context): Result {
            if(!NotificationManagerCompat.from(context).areNotificationsEnabled()) {
                return Result.MISSING_NOTIFICATION_PERMISSION
            }
            if(context.isServiceRunning(MonitoringService::class.java)) {
                return Result.RUNNING
            }
            val serviceIntent = Intent(context, MonitoringService::class.java)
            ContextCompat.startForegroundService(context, serviceIntent)
            return Result.STARTED
        }
    }
}