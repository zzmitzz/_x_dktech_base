package com.dktech.baseandroidviewdktech.ui.detail

import android.app.Activity
import android.content.Intent
import android.os.Handler
import android.os.Looper
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.lifecycleScope
import com.dktech.baseandroidviewdktech.base.BaseActivity
import com.dktech.baseandroidviewdktech.databinding.ActivityLoadingBinding
import com.dktech.baseandroidviewdktech.utils.helper.FileHelper
import com.dktech.baseandroidviewdktech.utils.helper.cvtFileNameIntoFillSVG
import com.dktech.baseandroidviewdktech.utils.helper.cvtFileNameIntoStrokeSVG
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream

class LoadingActivity : BaseActivity<ActivityLoadingBinding>() {
    private val client = OkHttpClient()

    override val onBackPressedCallback: OnBackPressedCallback
        get() =
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    finish()
                }
            }

    override fun getViewBinding(): ActivityLoadingBinding = ActivityLoadingBinding.inflate(layoutInflater)

    override fun initData() {

        val fileName = intent.getStringExtra(PAINTING_FILE_NAME)
        val fillSvgUrl = intent.getStringExtra(FILL_SVG_URL)
        val strokeSvgUrl = intent.getStringExtra(STROKE_SVG_URL)

        if (fileName != null && fillSvgUrl != null && strokeSvgUrl != null) {
            downloadSvgFiles(fileName, fillSvgUrl, strokeSvgUrl)
        } else {
            finishWithResult()
        }
    }

    private fun downloadSvgFiles(fileName: String, fillSvgUrl: String, strokeSvgUrl: String) {
        lifecycleScope.launch {
            withContext(Dispatchers.IO){
                try {
                    val fillFileName = cvtFileNameIntoFillSVG(fileName)
                    val strokeFileName = cvtFileNameIntoStrokeSVG(fileName)

                    val fillFile = File(cacheDir, fillFileName)
                    val strokeFile = File(cacheDir, strokeFileName)

                    downloadFile(fillSvgUrl, fillFile)
                    downloadFile(strokeSvgUrl, strokeFile)

                    withContext(Dispatchers.Main) {
                        finishWithResult()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    withContext(Dispatchers.Main) {
                        finishWithResult()
                    }
                }
            }
        }
    }

    private fun downloadFile(url: String, destinationFile: File) {
        val request = Request.Builder().url(url).build()
        client.newCall(request).execute().use { response ->
            if (response.isSuccessful) {
                response.body?.let { body ->
                    FileOutputStream(destinationFile).use { outputStream ->
                        outputStream.write(body.bytes())
                    }
                }
            } else {
                throw Exception("Failed to download file: ${response.code}")
            }
        }
    }

    private fun finishWithResult() {
        Handler(Looper.getMainLooper()).postDelayed(
            {
                setResult(Activity.RESULT_OK, Intent())
                finish()
            },
            500,
        )
    }

    override fun initView() {
    }

    override fun initEvent() {
    }

    override fun initObserver() {
    }

    companion object {
        const val PAINTING_FILE_NAME = "painting_file_name"
        const val FILL_SVG_URL = "fill_svg_url"
        const val STROKE_SVG_URL = "stroke_svg_url"
    }

}
