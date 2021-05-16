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

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Color
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.beesechurgers.parker.R

class NotificationHelper(context: Context) : ContextWrapper(context) {

    companion object {
        private const val CHANNEL_ID = "Parker"
        const val NOTIFICATION_ID = 619
    }

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannels()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createChannels() {
        getManager().createNotificationChannel(NotificationChannel(CHANNEL_ID, "Parker", NotificationManager.IMPORTANCE_HIGH).apply {
            this.lightColor = Color.BLUE
            this.setSound(null, null)
            this.enableVibration(true)
            this.enableLights(true)
        })
    }

    fun getManager() = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    fun getSessionStartedNotification(): Notification = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setContentTitle("Session Started")
            .setContentText("Your time has been started.\nPark well, drive safe !")
            .setColorized(true)
            .setSmallIcon(R.drawable.ic_round_local_parking_24)
            .setColor(ContextCompat.getColor(applicationContext, R.color.colorAccent))
            .build()
    } else {
        Notification.Builder(applicationContext)
            .setContentTitle("Session Started")
            .setContentText("Your time has been started.\nPark well, drive safe !")
            .setSmallIcon(R.drawable.ic_round_local_parking_24)
            .setColor(ContextCompat.getColor(applicationContext, R.color.colorAccent))
            .build()
    }

    fun getSessionCompletedNotification(content: String): Notification = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setContentTitle("Car Exited")
            .setContentText("You have exited the parking.\n\n$content")
            .setStyle(NotificationCompat.BigTextStyle())
            .setColorized(true)
            .setSmallIcon(R.drawable.ic_round_local_parking_24)
            .setColor(ContextCompat.getColor(applicationContext, R.color.colorAccent))
            .build()
    } else {
        Notification.Builder(applicationContext)
            .setContentTitle("Car Exited")
            .setContentText("You have exited the parking.\n\n$content")
            .setStyle(Notification.BigTextStyle())
            .setSmallIcon(R.drawable.ic_round_local_parking_24)
            .setColor(ContextCompat.getColor(applicationContext, R.color.colorAccent))
            .build()
    }
}