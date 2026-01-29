package com.dktech.baseandroidviewdktech.ui.finish

import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.dktech.baseandroidviewdktech.R
import com.dktech.baseandroidviewdktech.base.BaseActivity
import com.dktech.baseandroidviewdktech.data.local.AppDatabase
import com.dktech.baseandroidviewdktech.data.local.model.Paint
import com.dktech.baseandroidviewdktech.databinding.ActivityResultBinding
import com.dktech.baseandroidviewdktech.ui.home.MainActivity
import com.dktech.baseandroidviewdktech.utils.helper.cvtFileNameIntoThumbPNG
import com.dktech.baseandroidviewdktech.utils.helper.setSafeOnClickListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ResultActivity : BaseActivity<ActivityResultBinding>() {
    override val onBackPressedCallback: OnBackPressedCallback
        get() =
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    navigateToHome()
                }
            }

    override fun getViewBinding(): ActivityResultBinding = ActivityResultBinding.inflate(layoutInflater)

    private var fileName: String = ""
    private val paintDAO by lazy { AppDatabase.getDatabase(this).paintDAO() }
    private var loadedBitmap: Bitmap? = null

    override fun initData() {
        fileName = intent.getStringExtra(CONST_FILE_NAME) ?: ""
    }

    override fun initView() {
        loadImage()
        loadPaintData()
    }

    private fun loadImage() {
        val pngFileName = cvtFileNameIntoThumbPNG(fileName) ?: return
        val cacheFile = File(cacheDir, pngFileName)
        
        if (cacheFile.exists()) {
            Glide.with(this)
                .asBitmap()
                .load(cacheFile)
                .into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                        loadedBitmap = resource
                        binding.ivResult.setImageBitmap(resource)
                    }
                    override fun onLoadCleared(placeholder: Drawable?) {}
                })
        }
    }

    private fun loadPaintData() {
        lifecycleScope.launch {
            val paint = withContext(Dispatchers.IO) {
                paintDAO.getPaint(fileName).firstOrNull()
            }
            
            val finishedDate = if (paint != null) {
                paint.finishedDate
            } else {
                val currentTime = System.currentTimeMillis()
                withContext(Dispatchers.IO) {
                    paintDAO.insertFinishedPaint(Paint(fileName, currentTime))
                }
                currentTime
            }
            
            val formattedDate = formatDate(finishedDate)
            binding.tvPaintedOn.text = getString(R.string.painted_on) + " " + formattedDate
        }
    }

    private fun formatDate(timestamp: Long): String {
        val dateFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.ENGLISH)
        return dateFormat.format(Date(timestamp))
    }

    override fun initEvent() {
        binding.icShare.setSafeOnClickListener {
            shareImage()
        }
        binding.btnDownload.setSafeOnClickListener {
            downloadImage()
        }
        binding.btnHome.setSafeOnClickListener {
            navigateToHome()
        }
    }

    private fun shareImage() {
        val bitmap = loadedBitmap ?: return
        
        lifecycleScope.launch {
            val uri = withContext(Dispatchers.IO) {
                saveBitmapToCache(bitmap)
            }
            
            uri?.let {
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "image/png"
                    putExtra(Intent.EXTRA_STREAM, it)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                startActivity(Intent.createChooser(shareIntent, "Share Image"))
            }
        }
    }

    private fun saveBitmapToCache(bitmap: Bitmap): Uri? {
        return try {
            val cachePath = File(cacheDir, "shared_images")
            cachePath.mkdirs()
            val file = File(cachePath, "${fileName}_share.png")
            FileOutputStream(file).use { stream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            }
            FileProvider.getUriForFile(
                this,
                "${packageName}.provider",
                file
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun downloadImage() {
        val bitmap = loadedBitmap ?: return
        
        lifecycleScope.launch {
            val success = withContext(Dispatchers.IO) {
                saveImageToGallery(bitmap)
            }
            
            if (success) {
                Toast.makeText(this@ResultActivity, "Image saved to gallery", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this@ResultActivity, "Failed to save image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveImageToGallery(bitmap: Bitmap): Boolean {
        return try {
            val displayName = "${fileName}_${System.currentTimeMillis()}.png"
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, displayName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/ColorBook")
                }
                
                val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                uri?.let {
                    contentResolver.openOutputStream(it)?.use { stream ->
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                    }
                }
                uri != null
            } else {
                val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                val colorBookDir = File(picturesDir, "ColorBook")
                colorBookDir.mkdirs()
                val file = File(colorBookDir, displayName)
                FileOutputStream(file).use { stream ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                }
                val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
                mediaScanIntent.data = Uri.fromFile(file)
                sendBroadcast(mediaScanIntent)
                true
            }
        } catch (e: Exception) {
            false
        }
    }

    private fun navigateToHome() {
        Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(this)
        }
        finish()
    }

    override fun initObserver() {
    }

    companion object {
        const val CONST_FILE_NAME = "CONST_FILE_NAME"
    }
}
