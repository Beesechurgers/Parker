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
import android.app.PendingIntent
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.beesechurgers.parker.PaymentActivity
import com.beesechurgers.parker.R
import com.beesechurgers.parker.utils.Utils
import com.beesechurgers.parker.utils.Utils.formatAmount

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

    fun getSessionCompletedNotification(min: String?, amount: String?, paymentRequired: String?): Notification {
        val title = "Car Exited"
        val finalAmount = amount?.formatAmount()
        val content = "You have exited the parking.\n\nTime Elapsed: $min min(s)\n" +
            "Amount: \u20B9 $finalAmount"
        val pendingIntent = PendingIntent.getActivity(this, 0,
            Intent(this, PaymentActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                .putExtra("min", min ?: Utils.INVALID_STRING)
                .putExtra("amount", finalAmount ?: Utils.INVALID_STRING),
            PendingIntent.FLAG_UPDATE_CURRENT)

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val builder = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(content)
                .setStyle(NotificationCompat.BigTextStyle())
                .setColorized(true)
                .setSmallIcon(R.drawable.ic_round_local_parking_24)
                .setColor(ContextCompat.getColor(applicationContext, R.color.colorAccent))

            if (paymentRequired != null && paymentRequired == "yes") {
                builder.addAction(R.drawable.ic_baseline_payment_24, "Pay Now", pendingIntent)
            }

            builder.build()
        } else {
            val builder = Notification.Builder(applicationContext)
                .setContentTitle(title)
                .setContentText(content)
                .setStyle(Notification.BigTextStyle())
                .setSmallIcon(R.drawable.ic_round_local_parking_24)
                .setColor(ContextCompat.getColor(applicationContext, R.color.colorAccent))
                .addAction(Notification.Action.Builder(R.drawable.ic_baseline_payment_24, "Pay Now", pendingIntent).build())

            if (paymentRequired != null && paymentRequired == "yes") {
                builder.addAction(R.drawable.ic_baseline_payment_24, "Pay Now", pendingIntent)
            }

            builder.build()
        }
    }
}