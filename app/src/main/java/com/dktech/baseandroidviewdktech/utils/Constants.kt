package com.dktech.baseandroidviewdktech.utils

import android.graphics.Paint
import android.net.Uri
import androidx.annotation.DrawableRes
import androidx.annotation.RawRes
import com.dktech.baseandroidviewdktech.R

data class Painting(
    val id: Int = 1,
    val imageThumbRemote: String = "https://gs-jj-us-static.oss-accelerate.aliyuncs.com/u_file/1902/products/25/9ac904e4a7.jpg",
    val fileName: String = "line_3",
    val fillFileName: String = "line_3.svg",
    val strokeFileName: String = "line_3_stroke.svg"
)

// Damn thing live as application lifecycle
object Constants {
    const val configPreview: String = "PREFS_OPEN_PREVIEW"
    const val configVibration: String = "PREFS_OPEN_VIBRATION"
    val mockListData =
        buildList {
            repeat(10) {
                add(Painting(it))
            }
        }
}
