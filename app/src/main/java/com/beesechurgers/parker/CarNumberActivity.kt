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
import androidx.appcompat.app.AppCompatActivity
import com.beesechurgers.parker.utils.DatabaseConstants
import com.beesechurgers.parker.utils.PrefKeys
import com.beesechurgers.parker.utils.Utils
import com.beesechurgers.parker.utils.Utils.isNetworkConnected
import com.beesechurgers.parker.utils.Utils.valueEvenListener
import com.beesechurgers.parker.utils.putString
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_car_number.*

class CarNumberActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_car_number)

        val auth = FirebaseAuth.getInstance()
        val user = auth.currentUser
        if (user == null) {     // Return if user is NULL; Do not continue
            super.onBackPressed()
            return
        }

        val rootRef = FirebaseDatabase.getInstance().getReference(DatabaseConstants.USERS)

        reg_btn.setOnClickListener {
            val number = Utils.validateCarNumber(reg_car_number)
            if (number == Utils.INVALID_STRING) {
                return@setOnClickListener
            }

            if (!isNetworkConnected()) {
                Toast.makeText(this, "You're Offline", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            reg_btn.visibility = View.GONE
            reg_progress.visibility = View.VISIBLE
            handlerCarNumber(number, rootRef, user)
        }

        reg_logout_btn.setOnClickListener {
            if (!isNetworkConnected()) {
                Toast.makeText(this, "You're Offline", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.signOut()
            Utils.clearUserData(this)
            startActivity(Intent(this, SplashActivity::class.java)
                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK))
        }
    }

    // Add car number to user database
    private fun handlerCarNumber(number: String, rootRef: DatabaseReference, user: FirebaseUser) {
        var exists = false
        rootRef.valueEvenListener(onDataChange = { snap ->
            // Check if provided car number doesn't already exists
            for (child in snap.children) {
                if (child.child(DatabaseConstants.NUMBER_PLATE).value.toString() == number) {
                    reg_car_number.error = "Car number already exists"
                    exists = true

                    reg_btn.visibility = View.VISIBLE
                    reg_progress.visibility = View.GONE
                    break
                }
            }

            if (!exists) {
                rootRef.child(user.uid).updateChildren(HashMap<String, Any>().apply {
                    this[DatabaseConstants.NUMBER_PLATE] = number
                }).addOnCompleteListener {
                    if (it.isSuccessful) {
                        putString(PrefKeys.CAR_NUMBER, number)
                        startActivity(Intent(this, MainActivity::class.java)
                            .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK))
                    } else {
                        Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show()
                        reg_btn.visibility = View.VISIBLE
                        reg_progress.visibility = View.GONE
                    }
                }
            }
        })
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        recreate()
    }
}