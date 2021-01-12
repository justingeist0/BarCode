package com.fantasmaplasma.barcode

import android.Manifest
import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import com.fantasmaplasma.barcode.databinding.ActivityMainBinding
import com.google.zxing.Result
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import me.dm7.barcodescanner.zxing.ZXingScannerView

class MainActivity : Activity(), ZXingScannerView.ResultHandler {
    private lateinit var mBinding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
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

    override fun onResume() {
        super.onResume()
        val scanner = mBinding.zxscan
        scanner.setResultHandler(this)
        scanner.startCamera()
    }

    override fun handleResult(rawResult: Result?) {
        mBinding.layoutResult.text = rawResult?.text
        mBinding.zxscan.resumeCameraPreview(this)
    }

    override fun onPause() {
        super.onPause()
        val scanner = mBinding.zxscan
        scanner.stopCamera()
    }

}