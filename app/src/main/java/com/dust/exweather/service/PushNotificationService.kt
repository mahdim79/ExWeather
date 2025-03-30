package com.dust.exweather.service

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class PushNotificationService:FirebaseMessagingService() {

    private val TAG = "PushNotificationService"

    init{
        Log.i(TAG,"init")
    }

    override fun onNewToken(p0: String) {
        Log.i(TAG,"onNewToken: $p0")
        super.onNewToken(p0)
    }

    override fun onMessageReceived(p0: RemoteMessage) {
        Log.i(TAG,"onMessageReceived")
        super.onMessageReceived(p0)
    }
}