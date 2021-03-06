/*
 * Copyright (c) 2021, Beesechurgers <https://github.com/Beesechurgers>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 */

package com.beesechurgers.parker.appService

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * A firebase service to receive notification
 */
class FirebaseService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "FirebaseService"
    }

    override fun onNewToken(token: String) {
        Log.d(TAG, "onNewToken")
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            // Notify is message received is valid
            if (remoteMessage.data["to"] == user.uid) {
                val data = remoteMessage.data
                with(NotificationHelper(this)) {
                    Log.d(TAG, "onMessageReceived: Notifying user")
                    this.getManager().notify(NotificationHelper.NOTIFICATION_ID,
                        this.getSessionCompletedNotification(data["min"], data["amount"], data["paymentRequired"]))
                }
            }
        }
    }
}