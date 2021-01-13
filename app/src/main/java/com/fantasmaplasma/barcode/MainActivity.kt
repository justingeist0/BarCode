package com.fantasmaplasma.barcode

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.util.isNotEmpty
import com.fantasmaplasma.barcode.databinding.ActivityMainBinding
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import com.google.zxing.Result
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import me.dm7.barcodescanner.zxing.ZXingScannerView

class MainActivity : Activity() {
    private lateinit var mBinding: ActivityMainBinding
    private lateinit var cameraSource: CameraSource
    private lateinit var detector: BarcodeDetector

    private var isUsingZX = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        initBtnListener()
        initGoogleVision()
        initZxing()
        ensureCameraStart()
    }

    private fun initBtnListener() {
        with(mBinding.btnToggle) {
            setOnClickListener {
                stopCurrentScanner()
                isUsingZX = !isUsingZX
                text = getToggleBtnTxt()
                startCurrentScanner()
            }
            text = getToggleBtnTxt()
        }
    }

    private fun getToggleBtnTxt() =
            if(isUsingZX)
                "ZXing"
            else
                "Mobile Vision"

    private fun initGoogleVision() {
        detector = BarcodeDetector.Builder(this).build()
        cameraSource = CameraSource.Builder(this, detector)
                .setAutoFocusEnabled(true)
                .build()
        detector.setProcessor(googleVisionProcessor)
    }

    private val googleVisionProcessor = object : Detector.Processor<Barcode> {
        override fun release() {}
        override fun receiveDetections(detections: Detector.Detections<Barcode>) {
            if (detections.detectedItems.isNotEmpty()) {
                val barcode = detections.detectedItems.valueAt(0)
                handleRawBarcodeResult(barcode.rawValue)
            }
        }
    }

    private fun initZxing() {
        mBinding.zxscan.setResultHandler(zxingResultHandler)
    }

    private val zxingResultHandler = object :  ZXingScannerView.ResultHandler {
        override fun handleResult(rawResult: Result?) {
            rawResult?.apply { handleRawBarcodeResult(text) }
            mBinding.zxscan.resumeCameraPreview(this)
        }
    }

    private fun ensureCameraStart() {
        with(mBinding) {
            root.addOnLayoutChangeListener(object: View.OnLayoutChangeListener {
                override fun onLayoutChange(v: View?, left: Int, top: Int, right: Int, bottom: Int, oldLeft: Int, oldTop: Int, oldRight: Int, oldBottom: Int) {
                    btnToggle.callOnClick()
                    root.removeOnLayoutChangeListener(this)
                }
            })
        }
    }

    override fun onResume() {
        super.onResume()
        startCurrentScanner()
    }

    private fun startCurrentScanner() {
        when {
            needsCameraPermission() -> {
                acquireCameraPermission()
            }
            isUsingZX -> {
                startZXScanner()
            }
            else -> {
                startGoogleScanner()
            }
        }
    }

    private fun needsCameraPermission() =
            ActivityCompat.checkSelfPermission(this@MainActivity, Manifest.permission.CAMERA) !=
                    PackageManager.PERMISSION_GRANTED

    private fun acquireCameraPermission() {
        Dexter.withActivity(this)
                .withPermission(Manifest.permission.CAMERA)
                .withListener(object : PermissionListener {
                    override fun onPermissionGranted(response: PermissionGrantedResponse?) {}
                    override fun onPermissionDenied(response: PermissionDeniedResponse?) {
                        val context: Context = this@MainActivity
                        Toast.makeText(context, context.getText(R.string.we_need_camera), Toast.LENGTH_SHORT)
                                .show()
                    }
                    override fun onPermissionRationaleShouldBeShown(permission: PermissionRequest?, token: PermissionToken?) {}
                }).check()
    }

    private fun startZXScanner() {
        val scanner = mBinding.zxscan
        scanner.visibility = View.VISIBLE
        scanner.startCamera()
    }


    @SuppressLint("MissingPermission")
    private fun startGoogleScanner() {
        val scanner = mBinding.surfaceView
        scanner.visibility = View.VISIBLE
        cameraSource.start(scanner.holder)
    }

    override fun onPause() {
        super.onPause()
        stopCurrentScanner()
    }

    private fun stopCurrentScanner() {
        if(needsCameraPermission()) return
        if(isUsingZX) {
            val scanner = mBinding.zxscan
            scanner.stopCamera()
            scanner.visibility = View.INVISIBLE
        } else {
            val scanner = mBinding.surfaceView
            scanner.visibility = View.INVISIBLE
            cameraSource.stop()
        }
    }

    private fun handleRawBarcodeResult(result: String) {
        //TODO Do something with barcode data
        mBinding.layoutResult.text = result
    }

}