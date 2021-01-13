package com.fantasmaplasma.barcode

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.fantasmaplasma.barcode.databinding.ActivityMainBinding

const val BAR_CODE_ACTIVITY_REQUEST_CODE = 2
const val EXTRA_BAR_CODE_STR_ARRAY = "extra_result"

class MainActivity : AppCompatActivity() {
    private lateinit var mBinding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        initListener()
    }

    private fun initListener() {
        mBinding.btnScanBarCode.setOnClickListener {
            startBarCodeScanningActivity()
        }
    }

    private fun startBarCodeScanningActivity() {
        startActivityForResult(
                Intent(this, BarCodeActivity::class.java),
                BAR_CODE_ACTIVITY_REQUEST_CODE
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == BAR_CODE_ACTIVITY_REQUEST_CODE) {
            val rawData = data?.getStringArrayExtra(EXTRA_BAR_CODE_STR_ARRAY)
            handleBarCodeRawData(rawData)
        }
    }

    private fun handleBarCodeRawData(rawData: Array<String>?) {
        var resultStr = ""
        rawData?.forEach { barCodeData ->
            resultStr += "$barCodeData\n--------------\n"
        }
        mBinding.tvResult.text = resultStr
        //This function would be moved to viewModel
    }

}