package com.beesechurgers.parker

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Size
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.beesechurgers.parker.utils.QRCodeImageAnalyzer
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.android.synthetic.main.activity_scanner.*

class ScannerActivity : AppCompatActivity() {

    companion object {
        private const val CAMERA_CODE = 111
    }

    private var imageCapture: ImageCapture? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scanner)

        cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        handleCameraPermission()
    }

    private fun handleCameraPermission() {
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
        cameraProviderFuture.addListener({
            try {
                cameraProvider = cameraProviderFuture.get()
                bindCamera()
            } catch (e: Exception) {
                Toast.makeText(this, "Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun bindCamera() {
        val preview = Preview.Builder()/*.setTargetAspectRatio(AspectRatio.RATIO_4_3)*/
            .build().also { it.setSurfaceProvider(camera_preview.surfaceProvider) }

        imageCapture = ImageCapture.Builder().build()

        val imageAnalysis = ImageAnalysis.Builder().setTargetResolution(Size(1280, 720))
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build().apply {
                this.setAnalyzer(ContextCompat.getMainExecutor(this@ScannerActivity),
                    QRCodeImageAnalyzer { data ->
                        Toast.makeText(this@ScannerActivity, "QRCODE: $data", Toast.LENGTH_SHORT).show()
                        cameraProvider?.unbindAll()
                    })
            }

        cameraProvider?.unbindAll()
        cameraProvider?.bindToLifecycle(this, CameraSelector.DEFAULT_BACK_CAMERA, imageAnalysis, preview, imageCapture)
    }
}