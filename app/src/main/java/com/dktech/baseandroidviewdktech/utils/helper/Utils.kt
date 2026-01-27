package com.dktech.baseandroidviewdktech.utils.helper

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.graphics.Picture
import android.os.Build
import android.os.CombinedVibration
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.text.BoringLayout
import android.widget.Toast
import androidx.annotation.RequiresPermission
import androidx.core.content.edit
import com.caverock.androidsvg.SVG
import com.dktech.baseandroidviewdktech.base.ui_models.LanguageModel
import com.dktech.baseandroidviewdktech.base.ui_models.getLanguageList
import com.dktech.baseandroidviewdktech.utils.PersistentStorage
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.first
import java.io.InputStream

inline fun <reified T> Gson.fromJsonWithTypeToken(value: String): T =
    this.fromJson(value, object : TypeToken<T>() {}.type)

inline fun <reified T> Gson.toJsonWithTypeToken(obj: T): String =
    this.toJson(obj, object : TypeToken<T>() {}.type)

suspend fun getSelectedLanguage(context: Context): LanguageModel =
    PersistentStorage.Companion
        .getInstance(context)
        .readKey(PersistentStorage.Key.APPLICATION_LANGUAGE)
        .first()
        ?.let {
            Gson().fromJsonWithTypeToken<LanguageModel>(it)
        } ?: getLanguageList()[0]

fun setSelectedLanguage(
    context: Context,
    language: LanguageModel,
    onSuccess: () -> Unit = {},
) {
    PersistentStorage.Companion
        .getInstance(context)
        .saveKey(
            PersistentStorage.Key.APPLICATION_LANGUAGE,
            Gson().toJsonWithTypeToken(language),
            onSuccess
        )
}

private const val SHARE_PREFERENCES_TAG = "DKTechBase"

// ===================== BOOLEAN =====================

fun Context.getBooleanPrefs(
    key: String,
    default: Boolean = false,
): Boolean {
    val prefs = getSharedPreferences(SHARE_PREFERENCES_TAG, Context.MODE_PRIVATE)
    return prefs.getBoolean(key, default)
}

fun Context.setBooleanPrefs(
    key: String,
    value: Boolean,
) {
    val prefs = getSharedPreferences(SHARE_PREFERENCES_TAG, Context.MODE_PRIVATE)
    prefs.edit { putBoolean(key, value) }
}

// ===================== STRING =====================

fun Context.getStringPrefs(
    key: String,
    default: String? = null,
): String? {
    val prefs = getSharedPreferences(SHARE_PREFERENCES_TAG, Context.MODE_PRIVATE)
    return prefs.getString(key, default)
}

fun Context.setStringPrefs(
    key: String,
    value: String?,
) {
    val prefs = getSharedPreferences(SHARE_PREFERENCES_TAG, Context.MODE_PRIVATE)
    prefs.edit().putString(key, value).apply()
}

// ===================== INT =====================

fun Context.getIntPrefs(
    key: String,
    default: Int = 0,
): Int {
    val prefs = getSharedPreferences(SHARE_PREFERENCES_TAG, Context.MODE_PRIVATE)
    return prefs.getInt(key, default)
}

fun Context.setIntPrefs(
    key: String,
    value: Int,
) {
    val prefs = getSharedPreferences(SHARE_PREFERENCES_TAG, Context.MODE_PRIVATE)
    prefs.edit().putInt(key, value).apply()
}

// ===================== LONG =====================

fun Context.getLongPrefs(
    key: String,
    default: Long = 0L,
): Long {
    val prefs = getSharedPreferences(SHARE_PREFERENCES_TAG, Context.MODE_PRIVATE)
    return prefs.getLong(key, default)
}

fun Context.setLongPrefs(
    key: String,
    value: Long,
) {
    val prefs = getSharedPreferences(SHARE_PREFERENCES_TAG, Context.MODE_PRIVATE)
    prefs.edit().putLong(key, value).apply()
}

// ===================== FLOAT =====================

fun Context.getFloatPrefs(
    key: String,
    default: Float = 0f,
): Float {
    val prefs = getSharedPreferences(SHARE_PREFERENCES_TAG, Context.MODE_PRIVATE)
    return prefs.getFloat(key, default)
}

fun Context.setFloatPrefs(
    key: String,
    value: Float,
) {
    val prefs = getSharedPreferences(SHARE_PREFERENCES_TAG, Context.MODE_PRIVATE)
    prefs.edit().putFloat(key, value).apply()
}

// ===================== COMMON =====================

fun Context.removePrefs(key: String) {
    val prefs = getSharedPreferences(SHARE_PREFERENCES_TAG, Context.MODE_PRIVATE)
    prefs.edit().remove(key).apply()
}

fun Context.clearAllPrefs() {
    val prefs = getSharedPreferences(SHARE_PREFERENCES_TAG, Context.MODE_PRIVATE)
    prefs.edit().clear().apply()
}

@RequiresPermission(Manifest.permission.VIBRATE)
fun Context.vibrate(durationMs: Long = 50L) {
    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibrator = (getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager)
            vibrator.vibrate(
                CombinedVibration.createParallel(
                    VibrationEffect.createOneShot(
                        durationMs,
                        VibrationEffect.DEFAULT_AMPLITUDE,
                    ),
                ),
            )
        } else {
            val vibrator =
                getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

            vibrator.vibrate(
                VibrationEffect.createOneShot(
                    durationMs,
                    VibrationEffect.DEFAULT_AMPLITUDE,
                ),
            )
        }
    } catch (e: Exception) {
        Toast.makeText(this, "Vibration is not supported", Toast.LENGTH_SHORT).show()
    }
}

fun cvtFileNameIntoFillSVG(fileName: String): String = "$fileName.svg"

fun cvtFileNameIntoStrokeSVG(fileName: String): String = "${fileName}_stroke.svg"

fun cvtFileNameIntoThumbPNG(fileName: String?): String? {
    if (fileName == null) {
        return null
    }
    return "${fileName}.png"
}
