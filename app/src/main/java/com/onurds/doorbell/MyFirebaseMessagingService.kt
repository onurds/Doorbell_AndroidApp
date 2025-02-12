package com.onurds.doorbell  // Make sure this matches your package name

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class MyFirebaseMessagingService : FirebaseMessagingService() {
    private val scope = CoroutineScope(Dispatchers.IO + Job())

    // New FCM token is generation
    override fun onNewToken(token: String) {
        super.onNewToken(token)

        println("New FCM token: $token")
    }

    // New message arriving
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)


        createNotificationChannel()

        remoteMessage.notification?.let { notification ->
            // Log the notification
            scope.launch {
                val database = NotificationDatabase.getDatabase(applicationContext)
                database.notificationLogDao().insertLog(
                    NotificationLogEntity(
                        timestamp = LocalDateTime.now(),
                        message = notification.body ?: "Doorbell ring detected"
                    )
                )
            }

            showNotification(
                title = notification.title ?: "Doorbell Alert",
                message = notification.body ?: "Someone is at your door!"
            )
        }
    }


    private fun createNotificationChannel() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "doorbell_channel"
            val channelName = "Doorbell Alerts"
            val importance = NotificationManager.IMPORTANCE_HIGH

            // Create the NotificationChannel with all the parameters
            val channel = NotificationChannel(channelId, channelName, importance).apply {
                description = "Notifications for doorbell events"
                enableVibration(true)

            }

            // Register the channel with the system
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }


    private fun showNotification(title: String, message: String) {
        // Intent that opens the app when the notification is tapped
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }

        // PendingIntent that wraps the Intent
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        // Notification parameters
        val channelId = "doorbell_channel"
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        // Building the notification
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)  // Notification disappears when tapped
            .setSound(defaultSoundUri)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)

        // Show the notification
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Unique ID for each notification
        val notificationId = System.currentTimeMillis().toInt()
        notificationManager.notify(notificationId, notificationBuilder.build())
    }
}
