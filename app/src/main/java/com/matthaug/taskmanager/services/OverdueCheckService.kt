package com.matthaug.taskmanager.services

import android.app.*
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.google.gson.Gson
import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.reflect.TypeToken
import com.matthaug.taskmanager.R
import com.matthaug.taskmanager.models.Task
import java.io.File

class OverdueCheckService : Service() {

    private val channelId = "OverdueTaskChannel"

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(1, buildInitialNotification())
        //Check right away
        checkForOverdueTasks()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        //this is optional, however it is used to check for overdue tasks
        return START_NOT_STICKY
    }

    private fun checkForOverdueTasks() {
        val tasks = loadTasksFromFile()
        val overdueCount = tasks.count { it.overdue && !it.completed }

        if (overdueCount > 0) {
            //Building what the notif looks like
            val notification = NotificationCompat.Builder(this, channelId)
                .setContentTitle("Overdue Task Reminder")
                .setContentText("You have $overdueCount overdue task(s).")
                .setSmallIcon(R.drawable.ic_notification)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build()

            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(2, notification)
        }
    }

    private fun buildInitialNotification(): Notification {
        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Task Manager Running")
            .setContentText("Checking for overdue tasks...")
            .setSmallIcon(R.drawable.ic_air_plane)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                channelId,
                "Overdue Task Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    private fun loadTasksFromFile(): List<Task> {
        val file = File(filesDir, "tasks.json")
        if (!file.exists()) return emptyList()

        val json = file.readText()
        val type = object : TypeToken<List<Task>>() {}.type
        return Gson().fromJson(json, type)
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
