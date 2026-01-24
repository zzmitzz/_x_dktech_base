package com.dktech.baseandroidviewdktech.utils

import android.graphics.Paint
import android.net.Uri
import androidx.annotation.DrawableRes
import androidx.annotation.RawRes
import com.dktech.baseandroidviewdktech.R

data class Painting(
    val id: Int = 1,
    val imageThumbRemote: String = "https://gs-jj-us-static.oss-accelerate.aliyuncs.com/u_file/1902/products/25/9ac904e4a7.jpg",
    val imageLocal: Uri? = null,
)

// Damn thing live as application lifecycle
object Constants {
    val configPreview: String = "PREFS_OPEN_PREVIEW"
    val configVibration: String = "PREFS_OPEN_VIBRATION"
    val mockListData =
        buildList {
            repeat(10) {
                add(Painting(it))
            }
        }
}
