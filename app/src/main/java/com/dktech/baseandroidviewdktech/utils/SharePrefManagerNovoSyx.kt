package com.dktech.baseandroidviewdktech.utils

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.emulator.retro.console.game.BuildConfig
import com.google.gson.Gson

object SharePrefManagerNovoSyx {
    private const val SHARE_PREFERENCES_NAME = "${BuildConfig.APPLICATION_ID}.share_preferences"

    private var sharePreferences: SharedPreferences? = null

    fun initialize(application: Application) {
        sharePreferences = application.getSharedPreferences(SHARE_PREFERENCES_NAME, Context.MODE_PRIVATE)
    }

    fun put(key: String, value: String?) {
        if (sharePreferences == null) return

        sharePreferences!!.edit {
            putString(key, value)
        }
    }

    fun put(key: String, value: Boolean) {
        if (sharePreferences == null) return

        sharePreferences!!.edit {
            putBoolean(key, value)
        }
    }

    fun put(key: String, value: Int) {
        if (sharePreferences == null) return

        sharePreferences!!.edit {
            putInt(key, value)
        }
    }

    fun put(key: String, value: Float) {
        if (sharePreferences == null) return

        sharePreferences!!.edit {
            putFloat(key, value)
        }
    }

    fun put(key: String, value: Long) {
        if (sharePreferences == null) return

        sharePreferences!!.edit {
            putLong(key, value)
        }
    }

    inline fun <reified T> put(key: String, obj: T) {
        put(key, Gson().toJsonWithTypeToken(obj))
    }

    inline fun <reified T> put(key: String, obj: List<T>) {
        put(key, Gson().toJsonWithTypeToken<List<T>>(obj))
    }

    fun get(key: String, defaultValue: String? = null): String? {
        return sharePreferences?.getString(key, defaultValue) ?: defaultValue
    }

    fun get(key: String, defaultValue: Boolean): Boolean {
        return sharePreferences?.getBoolean(key, defaultValue) ?: defaultValue
    }

    fun get(key: String, defaultValue: Int): Int {
        return sharePreferences?.getInt(key, defaultValue) ?: defaultValue
    }

    fun get(key: String, defaultValue: Float): Float {
        return sharePreferences?.getFloat(key, defaultValue) ?: defaultValue
    }

    fun get(key: String, defaultValue: Long): Long {
        return sharePreferences?.getLong(key, defaultValue) ?: defaultValue
    }

    inline fun <reified T> get(key: String, defaultObj: T): T {
        return get(key)?.let {
            Gson().fromJsonWithTypeToken(it)
        } ?: defaultObj
    }

    inline fun <reified T> get(key: String, defaultObj: List<T>): List<T> {
        return get(key)?.let {
            Gson().fromJsonWithTypeToken<List<T>>(it)
        } ?: defaultObj
    }
}