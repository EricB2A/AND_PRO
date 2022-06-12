package com.example.blender

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat


class Notification private constructor() {

    fun showNotification(title: String, content: String) {
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

        with(NotificationManagerCompat.from(context!!)) {
            notify(0, notif.build())
        }
    }


    companion object {
        private var instance: Notification? = null
        private var context: Context? = null

        @Synchronized
        fun getInstance(context: Context? = null): Notification {
            if (instance == null && context != null) {
                this.context = context
                instance = Notification()
            }
            return instance!!
        }

    }
}