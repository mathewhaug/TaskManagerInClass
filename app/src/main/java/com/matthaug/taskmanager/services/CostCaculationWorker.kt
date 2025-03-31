package com.matthaug.taskmanager.services

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.gson.Gson
import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.reflect.TypeToken
import com.matthaug.taskmanager.models.Task
import java.io.File
import android.app.NotificationManager
import android.app.NotificationChannel
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.matthaug.taskmanager.R

class CostCaculationWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    //Overriding this function allws us to actually do background work
    override fun doWork(): Result {
        val tasks = loadTasksFromFile()
        val totalCost = tasks.filter { it.costAssociated }.sumOf { it.cost }

        // Show notification as this is the end of the work
        showNotification("Weekly Task Summary", "Total Task Cost: $totalCost")
        Log.d("CostCalculationWorker", "Total cost is: $totalCost")

        return Result.success()
    }

    private fun loadTasksFromFile(): List<Task> {
        val file = File(context.filesDir, "tasks.json")
        if (!file.exists()) return emptyList()

        val json = file.readText()
        val type = object : TypeToken<List<Task>>() {}.type
        return Gson().fromJson(json, type)
    }

    private fun showNotification(title: String, message: String) {
        //Same channel from before
        val channelId = "cost_notification_channel"
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Task Cost Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .build()

        manager.notify(3, notification)
    }
}
