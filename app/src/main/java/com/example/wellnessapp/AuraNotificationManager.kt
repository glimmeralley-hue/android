package com.example.wellnessapp

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat

class AuraNotificationManager(private val context: Context) {

    private val channelId = "AURA_CHANNEL"
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Aura Wellness"
            val descriptionText = "Personalized fitness and wellness alerts from Aura"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun sendAuraNotification(title: String, message: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Aura: $title")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setColor(0xFFFFFFFF.toInt())

        notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
    }

    // Automated alerts examples
    fun scheduleMetricReminder() {
        sendAuraNotification("Metric Pulse", "Your biostats are flatlining. Let's update your metrics to keep the evolution precise.")
    }

    fun scheduleHydrationAlert() {
        sendAuraNotification("Hydration Architect", "Optimal performance requires fluid precision. Have you reached your hydration target?")
    }
}
