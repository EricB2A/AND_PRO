package com.example.blender

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat


class Notification {

    companion object {

        fun createNotificationChannel(context: Context?){
            val name = "NEW_CONVERSATION" // Discussions
            val descriptionText ="New conversation" // Réception de messages normaux
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(MainActivity.CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                context?.getSystemService(Context.NOTIFICATION_SERVICE)
                        as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        fun showNotification(context: Context?, title: String, content: String) {
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
            val notif = NotificationCompat.Builder(context!!, MainActivity.CHANNEL_ID)
                .setSmallIcon(R.drawable.cake)
                .setContentTitle(title)
                .setContentIntent(pendingIntent)
                .setContentText(content)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)

            with(NotificationManagerCompat.from(context)) {
                notify(0, notif.build())
            }
        }


    }
}