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

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Size
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.beesechurgers.parker.appService.NotificationHelper
import com.beesechurgers.parker.utils.*
import com.beesechurgers.parker.utils.Utils.isNetworkConnected
import com.beesechurgers.parker.utils.Utils.isValidCarNumber
import com.beesechurgers.parker.utils.Utils.valueEvenListener
import com.google.common.util.concurrent.ListenableFuture
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_scanner.*

class ScannerActivity : AppCompatActivity() {

    companion object {
        private const val CAMERA_CODE = 111
    }

    private var imageCapture: ImageCapture? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>

    private lateinit var mAuth: FirebaseAuth
    private lateinit var mUser: FirebaseUser
    private lateinit var mActiveRef: DatabaseReference

    private var isScanning = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scanner)

        mAuth = FirebaseAuth.getInstance()
        val user = mAuth.currentUser
        if (user == null) {
            Toast.makeText(this, "FATAL: User is null", Toast.LENGTH_SHORT).show()
            super.onBackPressed()
        } else {
            mUser = user
        }
        mActiveRef = FirebaseDatabase.getInstance().getReference(DatabaseConstants.ACTIVE)

        cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        re_scan_btn.setOnClickListener {
            re_scan_btn.visibility = View.GONE
            scan_process.visibility = View.GONE
            scan_error_layout.visibility = View.GONE
            qr_scan_layout.visibility = View.VISIBLE
            scanned_data.text = ""
            handleCameraPermission()
        }
        handleCameraPermission()
    }

    private fun handleCameraPermission() {
        if (isScanning) return
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_CODE)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == CAMERA_CODE) {
            if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera()
            } else {
                Toast.makeText(this, "Camera Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun startCamera() {
        isScanning = true
        cameraProviderFuture.addListener({
            try {
                cameraProvider = cameraProviderFuture.get()
                bindCamera()
            } catch (e: Exception) {
                Toast.makeText(this, "Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    @SuppressLint("SetTextI18n")
    private fun bindCamera() {
        val preview = Preview.Builder()/*.setTargetAspectRatio(AspectRatio.RATIO_4_3)*/
            .build().also { it.setSurfaceProvider(camera_preview.surfaceProvider) }

        imageCapture = ImageCapture.Builder().build()

        val imageAnalysis = ImageAnalysis.Builder().setTargetResolution(Size(1280, 720))
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build().apply {
                this.setAnalyzer(ContextCompat.getMainExecutor(this@ScannerActivity),
                    QRCodeImageAnalyzer { data ->
                        if (!isNetworkConnected()) {
                            Toast.makeText(this@ScannerActivity, "You're Offline", Toast.LENGTH_SHORT).show()
                            return@QRCodeImageAnalyzer
                        }

                        cameraProvider?.unbindAll()

                        if (data.contains("/")) {
                            with(data.split("/")) {
                                val session = this[0]
                                val number = this[1]

                                qr_scan_layout.visibility = View.GONE
                                if (number.isValidCarNumber() && number == this@ScannerActivity.getString(PrefKeys.CAR_NUMBER)) {
                                    scanned_data.text = "Car Number: $number"
                                    scan_process.visibility = View.VISIBLE
                                    re_scan_btn.visibility = View.GONE

                                    isScanning = false
                                    startSession(session)
                                } else {
                                    scan_error_layout.visibility = View.VISIBLE
                                    re_scan_btn.visibility = View.VISIBLE
                                    isScanning = false
                                }
                            }
                        } else {
                            qr_scan_layout.visibility = View.GONE
                            scan_error_layout.visibility = View.VISIBLE
                            re_scan_btn.visibility = View.VISIBLE
                            isScanning = false
                        }
                    })
            }

        cameraProvider?.unbindAll()
        cameraProvider?.bindToLifecycle(this, CameraSelector.DEFAULT_BACK_CAMERA, imageAnalysis, preview, imageCapture)
    }

    private fun startSession(session: String) {
        FirebaseDatabase.getInstance().getReference(DatabaseConstants.USERS).child(mUser.uid).valueEvenListener(onDataChange = {
            val status = it.child(DatabaseConstants.CAR_STATUS).value
            val paymentStatus = it.child(DatabaseConstants.PAYMENT).child(DatabaseConstants.PAYMENT_STATUS).value

            if (status != null && paymentStatus != null) {
                if (status.toString() != DatabaseConstants.ENTERED) {
                    if (paymentStatus.toString() != DatabaseConstants.PAYMENT_PENDING) {
                        mActiveRef.child(mUser.uid).updateChildren(HashMap<String, Any>().apply {
                            this[DatabaseConstants.SESSION] = session
                        }).addOnCompleteListener {
                            with(NotificationHelper(this)) {
                                this.getManager().notify(NotificationHelper.NOTIFICATION_ID, this.getSessionStartedNotification())
                            }
                            super.onBackPressed()
                        }
                    } else {
                        qr_scan_layout.visibility = View.GONE
                        scan_process.visibility = View.GONE
                        scan_error_layout.visibility = View.VISIBLE
                        re_scan_btn.visibility = View.GONE

                        scan_error_text.text = getString(R.string.payment_pending_error)
                    }
                } else {
                    qr_scan_layout.visibility = View.GONE
                    scan_process.visibility = View.GONE
                    scan_error_layout.visibility = View.VISIBLE
                    re_scan_btn.visibility = View.GONE

                    scan_error_text.text = getString(R.string.car_already_entered_error)
                    sendCancelSessionNotification()
                }
            }
        })
    }

    private fun sendCancelSessionNotification() {
        FirebaseDatabase.getInstance().getReference("Server").child("Cancel").setValue(System.currentTimeMillis())
    }
}