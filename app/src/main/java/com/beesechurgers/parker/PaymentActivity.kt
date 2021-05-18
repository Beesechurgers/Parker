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

package com.beesechurgers.parker

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.beesechurgers.parker.appService.NotificationHelper
import com.beesechurgers.parker.utils.DatabaseConstants
import com.beesechurgers.parker.utils.PrefKeys
import com.beesechurgers.parker.utils.Utils
import com.beesechurgers.parker.utils.Utils.checkNull
import com.beesechurgers.parker.utils.Utils.isNetworkConnected
import com.beesechurgers.parker.utils.Utils.valueEvenListener
import com.beesechurgers.parker.utils.getString
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_payment.*

class PaymentActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment)

        if (!isNetworkConnected()) {
            Toast.makeText(this, "You're Offline", Toast.LENGTH_SHORT).show()
            super.onBackPressed()
            return
        }

        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            Toast.makeText(this, "FATAL: User NULL", Toast.LENGTH_SHORT).show()
            super.onBackPressed()
            return
        }

        val data = intent.extras
        if (data != null) {
            payment_amount.text = getString(R.string.payment_amount, data.getString("amount", Utils.INVALID_STRING))
            payment_time_elapsed.text = getString(R.string.payment_time_elapsed, data.getString("min", Utils.INVALID_STRING))
            payment_car_number.text = getString(R.string.payment_car_number, getString(PrefKeys.CAR_NUMBER))

            payment_loading_progress.visibility = View.GONE
            payment_layout.visibility = View.VISIBLE
        } else {
            loadFromDB(user.uid)
        }

        complete_payment.setOnClickListener {
            if (!isNetworkConnected()) {
                Toast.makeText(this, "You're Offline", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            completePayment(user.uid)
        }
    }

    private fun loadFromDB(uid: String) {
        FirebaseDatabase.getInstance().getReference(DatabaseConstants.USERS).child(uid).valueEvenListener(onDataChange = {
            checkNull(it.child(DatabaseConstants.ENTERED_TIME).value?.toString(),
                it.child(DatabaseConstants.EXITED_TIME).value?.toString(),
                it.child(DatabaseConstants.PAYMENT).child(DatabaseConstants.PAYMENT_STATUS).value?.toString(),
                it.child(DatabaseConstants.PAYMENT).child(DatabaseConstants.PAYMENT_AMOUNT).value?.toString()
            ) { (enter, exit, paymentStatus, paymentAmount) ->
                if (paymentStatus == DatabaseConstants.PAYMENT_COMPLETED) return@checkNull

                val min = (exit.toLong() - enter.toLong()) / 60
                val amount = paymentAmount.substring(IntRange(0, paymentAmount.indexOf('.') + 2))

                payment_amount.text = getString(R.string.payment_amount, amount)
                payment_time_elapsed.text = getString(R.string.payment_time_elapsed, min.toString())
                payment_car_number.text = getString(R.string.payment_car_number, getString(PrefKeys.CAR_NUMBER))

                payment_loading_progress.visibility = View.GONE
                payment_layout.visibility = View.VISIBLE
            }
        })
    }

    private fun completePayment(uid: String) {
        complete_payment.visibility = View.GONE
        pay_ongoing_progress.visibility = View.VISIBLE

        FirebaseDatabase.getInstance().getReference(DatabaseConstants.USERS).child(uid).updateChildren(HashMap<String, Any>().apply {
            this[DatabaseConstants.ENTERED_TIME] = DatabaseConstants.INVALID_TIME
            this[DatabaseConstants.EXITED_TIME] = DatabaseConstants.INVALID_TIME

            this[DatabaseConstants.PAYMENT] = HashMap<String, Any>().apply {
                this[DatabaseConstants.PAYMENT_AMOUNT] = 0.0
                this[DatabaseConstants.PAYMENT_STATUS] = DatabaseConstants.PAYMENT_COMPLETED
            }
        }).addOnCompleteListener {
            Toast.makeText(this, "Payment Successful", Toast.LENGTH_SHORT).show()
            NotificationHelper(this).getManager().cancel(NotificationHelper.NOTIFICATION_ID)
            startActivity(Intent(this, MainActivity::class.java)
                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK))
        }
    }
}