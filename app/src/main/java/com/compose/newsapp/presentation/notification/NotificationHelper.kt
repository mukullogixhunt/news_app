package com.compose.newsapp.presentation.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.compose.newsapp.presentation.MainActivity
import com.compose.newsapp.R
import com.compose.newsapp.core.util.Logger
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        private const val CHANNEL_ID = "newsflow_sync_channel_1" // Unique ID
        private const val CHANNEL_NAME = "News Updates"
        private const val NOTIFICATION_ID = 1001 // Unique ID for this notification type
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = "Notifications for new articles fetched in background"
                // Configure other channel features (lights, vibration) if desired
                // channel.enableLights(true)
                // channel.lightColor = Color.RED
                // channel.enableVibration(true)
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
            Logger.d("NotificationHelper", "Notification channel created: $CHANNEL_ID")
        }
    }

    fun showSyncNotification(title: String, content: String) {
        Logger.d("NotificationHelper", "Attempting to show notification: '$title' - '$content'")

        // Check for permission first (crucial for Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Logger.w(
                    "NotificationHelper",
                    "Cannot show notification - POST_NOTIFICATIONS permission not granted."
                )
                // Optionally: Queue the notification request? Or just skip.
                return // Exit if permission not granted
            }
        }

        // Intent to launch MainActivity when notification is tapped
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            // Example: Add extra to navigate to headlines tab/fragment upon opening
            // intent.putExtra("destination_id", R.id.navigation_headlines) // Assuming you have this ID in nav graph
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // IMPORTANT: Use a real notification icon drawable
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent) // Intent to launch on tap
            .setAutoCancel(true) // Dismiss notification on tap
        // Optional: Add actions, style, etc.
        // .addAction(R.drawable.ic_..., "Action Text", actionPendingIntent)
        // .setStyle(NotificationCompat.BigTextStyle().bigText("Longer content..."))

        // Get an instance of NotificationManagerCompat
        val notificationManager = NotificationManagerCompat.from(context)

        // notificationId is a unique int for each notification that you must define
        notificationManager.notify(NOTIFICATION_ID, builder.build())
        Logger.d("NotificationHelper", "Notification shown with ID: $NOTIFICATION_ID")
    }

}