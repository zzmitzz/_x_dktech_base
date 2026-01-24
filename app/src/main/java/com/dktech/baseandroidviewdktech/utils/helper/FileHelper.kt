package com.dktech.baseandroidviewdktech.utils.helper

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Picture
import com.caverock.androidsvg.SVG
import java.io.IOException

class FileHelper(
    val context: Context,
) {
    fun parseAssetFileToPicture(fileName: String): Picture {
        val inputStream = context.assets.open(fileName)
        val svg = SVG.getFromInputStream(inputStream)
        inputStream.close()
        return svg.renderToPicture()
    }

    fun parseAssetPNGFile(fileName: String): Bitmap? =
        try {
            context.assets.open(fileName).use { inputStream ->
                BitmapFactory.decodeStream(inputStream)
            }
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
}
