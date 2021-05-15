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
import androidx.appcompat.app.AppCompatActivity
import com.beesechurgers.parker.utils.PrefKeys
import com.beesechurgers.parker.utils.Utils
import com.beesechurgers.parker.utils.getString
import com.google.firebase.auth.FirebaseAuth
import com.squareup.picasso.Callback
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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

        scanner_fab.setOnClickListener { startActivity(Intent(this, ScannerActivity::class.java)) }

        logout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            Utils.clearUserData(this)
            startActivity(Intent(this, SplashActivity::class.java)
                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK))
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        recreate()
    }
}