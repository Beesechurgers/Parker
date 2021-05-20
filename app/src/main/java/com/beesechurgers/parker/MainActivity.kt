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
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.beesechurgers.parker.utils.DatabaseConstants
import com.beesechurgers.parker.utils.PrefKeys
import com.beesechurgers.parker.utils.Utils
import com.beesechurgers.parker.utils.Utils.formatAmount
import com.beesechurgers.parker.utils.Utils.isNetworkConnected
import com.beesechurgers.parker.utils.Utils.valueEvenListener
import com.beesechurgers.parker.utils.getString
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Callback
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var mUserRef: DatabaseReference
    private var mUser: FirebaseUser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mAuth = FirebaseAuth.getInstance()
        mUser = mAuth.currentUser
        if (mUser == null) {
            Toast.makeText(this, "FATAL: User NULL", Toast.LENGTH_SHORT).show()
            super.onBackPressed()
            return
        }
        mUserRef = FirebaseDatabase.getInstance().getReference(DatabaseConstants.USERS).child(mUser!!.uid)

        val url = getString(PrefKeys.USER_PHOTO)
        if (url != Utils.INVALID_STRING) {
            Picasso.get().load(url).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.ic_outline_account_circle_24)
                .into(main_profile_pic, object : Callback {
                    override fun onSuccess() = Unit

                    override fun onError(e: Exception?) {
                        Picasso.get().load(url).placeholder(R.drawable.ic_outline_account_circle_24).into(main_profile_pic)
                    }
                })
        }

        scanner_fab.setOnClickListener {
            if (!isNetworkConnected()) {
                Toast.makeText(this, "You're Offline", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            startActivity(Intent(this, ScannerActivity::class.java))
        }

        logout_fab.setOnClickListener {
            AlertDialog.Builder(this, R.style.AppDialogTheme)
                .setTitle("Are you sure you want to logout ?")
                .setPositiveButton("Logout") { dialog, _ ->
                    if (!isNetworkConnected()) {
                        Toast.makeText(this, "You're Offline", Toast.LENGTH_SHORT).show()
                        return@setPositiveButton
                    }

                    FirebaseAuth.getInstance().signOut()
                    Utils.clearUserData(this)
                    dialog.dismiss()
                    startActivity(Intent(this, SplashActivity::class.java)
                        .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK))
                }.setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
                .show()
        }

        change_payment_method.setOnClickListener { Toast.makeText(this, "Dummy Payment", Toast.LENGTH_SHORT).show() }
        payment_history_card.setOnClickListener {
            if (!isNetworkConnected()) {
                Toast.makeText(this, "You're Offline", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            startActivity(Intent(this, HistoryActivity::class.java))
        }

        refreshStatus()
    }

    private fun refreshStatus() {
        mUserRef.valueEvenListener(onDataChange = {
            // Default: this card should not be visible
            live_session_card.visibility = View.GONE

            // Show this card with data only when [DatabaseConstants.CAR_STATUS] is ENTERED
            val carStatus = it.child(DatabaseConstants.CAR_STATUS).value.toString()
            if (carStatus == DatabaseConstants.ENTERED) {

                // Ongoing Session
                val timeElapsed = ((System.currentTimeMillis() / 1000) - it.child(DatabaseConstants.ENTERED_TIME).value.toString().toLong()) / 60
                live_session_time.text = getString(R.string.live_session_time_elapsed, timeElapsed.toString())
                live_session_car_number.text = getString(PrefKeys.CAR_NUMBER)

                val amount = getEstimatedAmount(timeElapsed).toString()
                live_payment_amount.text = getString(R.string.payment_amount, amount.formatAmount())

                live_session_card.visibility = View.VISIBLE
            }

            // Default: this card should not be visible
            pending_payment_card.visibility = View.GONE

            // Show this car only when payment is pending
            val paymentStatus = it.child(DatabaseConstants.PAYMENT).child(DatabaseConstants.PAYMENT_STATUS).value.toString()
            if (paymentStatus == DatabaseConstants.PAYMENT_PENDING) {
                val amount = it.child(DatabaseConstants.PAYMENT).child(DatabaseConstants.PAYMENT_AMOUNT).value.toString()
                pending_payment_amount.text = getString(R.string.payment_amount, amount.formatAmount())
                pending_pay_now.setOnClickListener {
                    if (!isNetworkConnected()) {
                        Toast.makeText(this, "You're Offline", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }
                    startActivity(Intent(this, PaymentActivity::class.java))
                }
                pending_payment_card.visibility = View.VISIBLE
            }
        })
    }

    /**
     * Get estimated payment amount from the time user entered
     * till current (i.e. the time this function is called)
     *
     * @param timeElapsed current_time - entered_time
     */
    private fun getEstimatedAmount(timeElapsed: Long): Double {
        var payment: Double = 0.0
        var time = timeElapsed
        if (time >= 60) {
            val multiple = time / 60
            payment += 50.0 * multiple
            time -= 60 * multiple
        }
        payment += 10.0 * time / 15.0
        return payment
    }

    override fun onResume() {
        super.onResume()
        refreshStatus()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        recreate()
    }
}