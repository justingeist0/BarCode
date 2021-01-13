package com.fantasmaplasma.barcode

import androidx.appcompat.app.AppCompatActivity
import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.SurfaceHolder
import android.widget.Toast
import androidx.core.util.isNotEmpty
import com.fantasmaplasma.barcode.databinding.ActivityBarCodeBinding
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener

class BarCodeActivity : AppCompatActivity() {
    private lateinit var mBinding: ActivityBarCodeBinding
    private lateinit var mCameraSource: CameraSource
    private lateinit var mBarcodeDetector: BarcodeDetector

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityBarCodeBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        acquireCameraPermission()
    }

    private fun acquireCameraPermission() {
        Dexter.withActivity(this)
                .withPermission(Manifest.permission.CAMERA)
                .withListener(object : PermissionListener {
                    override fun onPermissionDenied(response: PermissionDeniedResponse?) { tellUserWeNeedCamera() }
                    override fun onPermissionGranted(response: PermissionGrantedResponse?) { initGoogleVisionScanner() }
                    override fun onPermissionRationaleShouldBeShown(permission: PermissionRequest?, token: PermissionToken?) {}
                }).check()
    }

    private fun tellUserWeNeedCamera() {
        Toast.makeText(this, getText(R.string.we_need_camera), Toast.LENGTH_SHORT)
                .show()
    }

    private fun initGoogleVisionScanner() {
        mBarcodeDetector = BarcodeDetector.Builder(this).build()
        mCameraSource = CameraSource.Builder(this, mBarcodeDetector)
                .setAutoFocusEnabled(true)
                .build()
        mBarcodeDetector.setProcessor(googleVisionProcessor)
        mBinding.surfaceView.holder.addCallback(surfaceCallback)
        showInstructions()
    }

    private fun showInstructions() {
        mBinding.tvBarCodeInstructions.translationY = mBinding.root.height/4f
        mBinding.tvBarCodeInstructions
            .animate()
            .translationY(0f)
            .alpha(1f)
            .start()
    }

    private val googleVisionProcessor = object : Detector.Processor<Barcode> {
        override fun release() {}
        override fun receiveDetections(detections: Detector.Detections<Barcode>) {
            if (detections.detectedItems.isNotEmpty()) {
                val results = Array<String>(detections.detectedItems.size()) { idx ->
                    detections.detectedItems.valueAt(idx).rawValue
                }
                handleRawBarcodeResults(results)
            }
        }
    }

    private val surfaceCallback = object : SurfaceHolder.Callback {
        @SuppressLint("MissingPermission")
        override fun surfaceCreated(holder: SurfaceHolder) { mCameraSource.start(holder) }
        override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}
        override fun surfaceDestroyed(holder: SurfaceHolder) { mCameraSource.stop() }
    }

    private fun handleRawBarcodeResults(result: Array<String>) {
        setResult(BAR_CODE_ACTIVITY_REQUEST_CODE,
                Intent().apply {
                    putExtra(EXTRA_BAR_CODE_STR_ARRAY, result)
                }
        )
        finish()
    }

}