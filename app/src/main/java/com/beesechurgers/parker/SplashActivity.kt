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
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.beesechurgers.parker.utils.*
import com.beesechurgers.parker.utils.Utils.isValidCarNumber
import com.beesechurgers.parker.utils.Utils.valueEvenListener
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_splash.*

class SplashActivity : AppCompatActivity() {

    companion object {
        private const val RC_SIGN_IN = 619
        private const val TAG = "SplashActivity"
    }

    private lateinit var mAuth: FirebaseAuth
    private lateinit var mRootRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        mAuth = FirebaseAuth.getInstance()
        mRootRef = FirebaseDatabase.getInstance().getReference(DatabaseConstants.USERS)
        val googleSignInClient = GoogleSignIn.getClient(this, GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build())

        if (mAuth.currentUser != null) {
            startActivity(Intent(this, if (getString(PrefKeys.CAR_NUMBER).isValidCarNumber()) {
                MainActivity::class.java
            } else CarNumberActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK))
        } else {
            login_layout.visibility = View.VISIBLE
        }

        sign_in_google.setOnClickListener {
            sign_in_google.visibility = View.GONE
            login_progress.visibility = View.VISIBLE

            googleSignInClient.signOut()
            startActivityForResult(googleSignInClient.signInIntent, RC_SIGN_IN)
        }
    }

    private fun handleUser(name: String, user: FirebaseUser) {
        mRootRef.valueEvenListener(onDataChange = { rootSnap ->
            Toast.makeText(this@SplashActivity, "Welcome $name", Toast.LENGTH_SHORT).show()
            if (rootSnap.hasChild(user.uid)) {
                Log.d(TAG, "onDataChange: User exists")

                mRootRef.child(user.uid).valueEvenListener(onDataChange = {
                    with(it.child(DatabaseConstants.NUMBER_PLATE).value.toString()) {
                        if (this.isValidCarNumber()) {
                            putString(PrefKeys.CAR_NUMBER, this)
                            startActivity(Intent(this@SplashActivity, MainActivity::class.java)
                                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK))
                        } else {
                            startActivity(Intent(this@SplashActivity, CarNumberActivity::class.java)
                                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK))
                        }
                    }
                })
            } else {
                Log.d(TAG, "onDataChange: New user")

                mRootRef.child(user.uid).updateChildren(
                    HashMap<String, Any>().apply {
                        this[DatabaseConstants.NUMBER_PLATE] = Utils.INVALID_STRING
                        this[DatabaseConstants.CAR_STATUS] = DatabaseConstants.EXITED
                        this[DatabaseConstants.ENTERED_TIME] = DatabaseConstants.INVALID_TIME
                        this[DatabaseConstants.EXITED_TIME] = DatabaseConstants.INVALID_TIME

                        this[DatabaseConstants.PAYMENT] = HashMap<String, Any>().apply {
                            this[DatabaseConstants.PAYMENT_AMOUNT] = 0.0
                            this[DatabaseConstants.PAYMENT_STATUS] = DatabaseConstants.PAYMENT_COMPLETED
                        }
                    }
                ).addOnCompleteListener {
                    if (it.isSuccessful) {
                        startActivity(Intent(this@SplashActivity, CarNumberActivity::class.java)
                            .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK))
                    }
                }
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                if (account != null) {
                    mAuth.signInWithCredential(GoogleAuthProvider.getCredential(account.idToken, null))
                        .addOnCompleteListener {
                            if (it.isSuccessful) {
                                val photoUrl = account.photoUrl
                                if (photoUrl != null) {
                                    putString(PrefKeys.USER_PHOTO, photoUrl.toString())
                                }

                                val user = mAuth.currentUser
                                if (user != null) {
                                    handleUser(account.givenName.toString(), user)
                                }
                            } else {
                                Toast.makeText(this, "Login failed", Toast.LENGTH_SHORT).show()
                                sign_in_google.visibility = View.VISIBLE
                                login_progress.visibility = View.GONE
                            }
                        }
                } else {
                    Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show()
                    sign_in_google.visibility = View.VISIBLE
                    login_progress.visibility = View.GONE
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this, "Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                sign_in_google.visibility = View.VISIBLE
                login_progress.visibility = View.GONE
            }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        recreate()
    }
}