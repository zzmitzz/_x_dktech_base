package com.dktech.baseandroidviewdktech.utils

import android.graphics.Paint
import android.net.Uri
import androidx.annotation.DrawableRes
import androidx.annotation.RawRes
import com.dktech.baseandroidviewdktech.R
import com.dktech.baseandroidviewdktech.model.Painting
import com.dktech.baseandroidviewdktech.ui.home.model.PaintingUIWrapper

// Damn thing live as application lifecycle
object Constants {
    const val CONFIG_PREVIEW: String = "PREFS_OPEN_PREVIEW"
    const val CONFIG_VIBRATION: String = "PREFS_OPEN_VIBRATION"
    val mockListData =
        buildList {
            repeat(10) {
                add(
                    PaintingUIWrapper(
                        remoteThumb = "https://opengameart.org/sites/default/files/oga-textures/115038/templategrid_orm.png",
                        cacheThumb = null,
                        fileName = "demon_1",
                    ),
                )
            }
        }
}
