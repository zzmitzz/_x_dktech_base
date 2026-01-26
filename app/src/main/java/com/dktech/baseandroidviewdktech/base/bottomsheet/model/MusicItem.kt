package com.dktech.baseandroidviewdktech.base.bottomsheet.model

data class MusicItem(
    val name: String,
    val artistName: String,
    val resourceId: Int? = null, // Cho nhạc local từ raw
    val filePath: String? = null,
)
