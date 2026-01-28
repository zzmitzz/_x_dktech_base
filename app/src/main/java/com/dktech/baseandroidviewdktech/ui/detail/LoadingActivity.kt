package com.dktech.baseandroidviewdktech.ui.detail

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.dktech.baseandroidviewdktech.base.BaseActivity
import com.dktech.baseandroidviewdktech.base.dialog.CollectionDialog
import com.dktech.baseandroidviewdktech.databinding.ActivityLoadingBinding
import com.dktech.baseandroidviewdktech.ui.home.model.PaintingUIWrapper
import com.dktech.baseandroidviewdktech.utils.helper.CustomLoadingImage
import com.dktech.baseandroidviewdktech.utils.helper.FileHelper
import com.dktech.baseandroidviewdktech.utils.helper.cvtFileNameIntoFillSVG
import com.dktech.baseandroidviewdktech.utils.helper.cvtFileNameIntoStrokeSVG
import com.dktech.baseandroidviewdktech.utils.helper.fromJsonWithTypeToken
import com.google.gson.Gson
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

    private var paintingUIWrapper: PaintingUIWrapper? = null

    override fun initData() {
        val serializedPaint =
            intent.getStringExtra(
                LoadingActivity.PAINTING,
            )
        serializedPaint?.let {
            try {
                paintingUIWrapper =
                    Gson().fromJsonWithTypeToken<PaintingUIWrapper>(serializedPaint)

                if (paintingUIWrapper?.cacheThumb == null) {
                    downloadSvgFiles(paintingUIWrapper?.fileName!!, paintingUIWrapper?.fillSVG!!, paintingUIWrapper?.strokeSVG!!)
                } else {
                    finishWithResult()
                }
            } catch (e: Exception) {
                handleErrorDownloading()
            }
        }
    }

    private fun handleErrorDownloading() {
        Toast
            .makeText(
                this@LoadingActivity,
                "Can't download resources, please try again later",
                Toast.LENGTH_SHORT,
            ).show()
        finish()
    }

    private fun downloadSvgFiles(
        fileName: String,
        fillSvgUrl: String,
        strokeSvgUrl: String,
    ) {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
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
                        handleErrorDownloading()
                    }
                }
            }
        }
    }

    private fun downloadFile(
        url: String,
        destinationFile: File,
    ) {
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
                Intent(this, DrawingActivity::class.java).apply {
                    putExtra(DrawingActivity.PAINTING_FILE_NAME, paintingUIWrapper?.fileName)
                    startActivity(this)
                }
                finish()
            },
            500,
        )
    }

    override fun initView() {
        CustomLoadingImage.loadImage(
            paintingUIWrapper!!,
            binding.imThumb,
            null,
        )
    }

    override fun initEvent() {
    }

    override fun initObserver() {
    }

    companion object {
        const val PAINTING = "painting_file_name"
    }
}
