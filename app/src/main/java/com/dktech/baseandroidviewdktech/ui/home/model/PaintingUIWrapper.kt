package com.dktech.baseandroidviewdktech.ui.home.model

import android.net.Uri
import kotlinx.serialization.Serializable

@Serializable
data class PaintingUIWrapper(
    val remoteThumb: String?,
    val cacheThumb: String?,
    val fileName: String,
    val fillSVG: String?,
    val strokeSVG: String?,
)
