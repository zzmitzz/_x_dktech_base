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

    var ON_RESUME_ENABLE = "0"
    var id_ONRESUME = ""
    var check_test_ad = "0"
    var IS_DEBUG = "0"
    var KEY_SOLAR = ""
    var POPUP_UPDATE_VERSION = "0"
    var JSON_INTRO_1 = "0"
    var JSON_INTRO_2 = "0"
    var JSON_INTRO_3 = "0"

    var isInterHomeFirstClick: Boolean = true
    var isInterDetailFirstClick: Boolean = true

    var JSON_FULL_INTRO_1 = "0"
    var JSON_FULL_INTRO_2 = "0"
}
