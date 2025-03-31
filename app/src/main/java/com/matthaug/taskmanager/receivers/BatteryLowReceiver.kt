package com.matthaug.taskmanager.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.matthaug.taskmanager.R

class BatteryLowReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        //Optional Show a Toast
        Toast.makeText(context, "Battery is low – pause background syncing?", Toast.LENGTH_LONG).show()

        //Show a notification instead of just a Toast
        val notification = NotificationCompat.Builder(context, "battery_channel")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Battery Low")
            .setContentText("Battery is low – pause background syncing to save power?")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        NotificationManagerCompat.from(context).notify(101, notification)
    }
}

