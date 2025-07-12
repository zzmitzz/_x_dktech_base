package com.dktech.baseandroidviewdktech.utils

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.dktech.baseandroidviewdktech.appDataStore
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlin.math.log


class PersistentStorage private constructor(context: Context) {

    object Key {
        const val PREFERENCE_KEY = "PREFERENCE_KEY"
        val APPLICATION_THEME = stringPreferencesKey("AppTheme")
        val APPLICATION_LANGUAGE = stringPreferencesKey("AppLanguage")
    }

    private val dataStore = context.appDataStore
    private val scope = CoroutineScope(Dispatchers.IO) + CoroutineExceptionHandler { _, exception ->
        logger(exception.message.toString())
    }

    companion object {
        @Volatile
        private var instance: PersistentStorage? = null

        fun getInstance(context: Context): PersistentStorage {
            return instance ?: synchronized(this) {
                instance ?: PersistentStorage(context).also { instance = it }
            }
        }
    }

    fun saveKey(key: Preferences.Key<String>, value: String, onSuccess: (() -> Unit)? = null) {
        scope.launch {
            dataStore.edit {
                it[key] = value
            }
        }
    }

    fun readKey(key: Preferences.Key<String>): Flow<String?> = dataStore.data.map { it[key] }

    fun clearKey(vararg keys: Preferences.Key<String>) {
        scope.launch {
            keys.forEach { key ->
                dataStore.edit { it.remove(key) }
            }
        }
    }

    private fun logger(msg: String) {
        Log.d(this::class.java.simpleName.toString(), msg)
    }


}