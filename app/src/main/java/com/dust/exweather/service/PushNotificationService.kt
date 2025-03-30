package com.dust.exweather.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.dust.exweather.R
import com.dust.exweather.ui.activities.SplashActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class PushNotificationService : FirebaseMessagingService() {

    private val TAG = "PushNotificationService"

    private val fcmNotificationId = 10050

    private val notificationManager by lazy {
        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    init {
        Log.i(TAG,"firebase service initialization")
    }

    override fun onNewToken(p0: String) {
        super.onNewToken(p0)
    }

    override fun onMessageReceived(p0: RemoteMessage) {
        super.onMessageReceived(p0)
        Log.i(TAG,"onMessageReceived")

        var title = "Havaye man"
        var content = "No Content"
        p0.data.keys.forEach {
            when (it) {
                "HAVAYE_MAN_PUSH_TITLE" -> {
                    p0.data[it]?.let { t ->
                        title = t
                    }
                }

                "HAVAYE_MAN_PUSH_CONTENT" -> {
                    p0.data[it]?.let { c ->
                        content = c
                    }
                }
            }
        }
        showNotificationToUser(title, content)
    }

    private fun showNotificationToUser(title: String, content: String) {
        Log.i(TAG,"showNotificationToUser: $title $content")

        val contentIntent = Intent(this, SplashActivity::class.java)
        contentIntent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP

        val notification = NotificationCompat.Builder(this, createNotificationChannel())
            .setSmallIcon(R.drawable.ic_launcher)
            .setContentTitle(title)
            .setContentText(content)
            .setOngoing(false)
            .setAutoCancel(true)
            .setContentIntent(
                PendingIntent.getActivity(
                    this,
                    201,
                    contentIntent,
                    PendingIntent.FLAG_IMMUTABLE
                )
            )
            .setVibrate(LongArray(3) { 200L })
            .build()

        notificationManager.notify(fcmNotificationId, notification)
    }

    private fun createNotificationChannel(): String {
        val channelId = getString(R.string.default_notification_channel_id)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "havayeman_notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            channel.lightColor = Color.BLUE
            channel.vibrationPattern = longArrayOf(0L)
            channel.enableVibration(false)
            channel.setSound(null, null)

            notificationManager.createNotificationChannel(channel)
        }
        return channelId
    }

}