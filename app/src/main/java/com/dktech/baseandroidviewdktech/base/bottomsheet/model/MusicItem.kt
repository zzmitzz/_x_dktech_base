package com.dktech.baseandroidviewdktech.base.bottomsheet.model

import androidx.annotation.DrawableRes

data class MusicItem(
    val name: String,
    val artistName: String,
    val resourceId: Int? = null,
    val filePath: String? = null,
    @DrawableRes val thumbnail: Int,
)
