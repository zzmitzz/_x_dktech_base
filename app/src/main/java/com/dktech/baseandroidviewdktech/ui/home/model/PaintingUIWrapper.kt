package com.dktech.baseandroidviewdktech.ui.home.model

import android.net.Uri

data class PaintingUIWrapper(
    val remoteThumb: String,
    val cacheThumb: Uri?,
    val fileName: String,
    val fillSVG: String?,
    val strokeSVG: String?
)
