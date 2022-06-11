package com.example.blender

import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.blender.BLE.BLEClient

class Notification private constructor() {

    fun showNotification(title: String, content: String) {
        val notif = NotificationCompat.Builder(context!!, CHANNEL_ID)
            .setSmallIcon(R.drawable.cake)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        with(NotificationManagerCompat.from(context!!)) {
            notify(0, notif.build())
        }
    }

    companion object {
        private var instance: Notification? = null
        private var context: Context? = null
        private const val CHANNEL_ID = "Blender"

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