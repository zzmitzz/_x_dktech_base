package com.dktech.baseandroidviewdktech.utils

import android.content.Context
import com.dktech.baseandroidviewdktech.base.ui_models.LanguageModel
import com.dktech.baseandroidviewdktech.base.ui_models.getLanguageList
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first


inline fun <reified T> Gson.fromJsonWithTypeToken(value: String): T {
    return this.fromJson(value, object : TypeToken<T>() {}.type)
}

inline fun <reified T> Gson.toJsonWithTypeToken(obj: T): String {
    return this.toJson(obj, object : TypeToken<T>() {}.type)
}

suspend fun getSelectedLanguage(context: Context): LanguageModel {
    return PersistentStorage.getInstance(context)
        .readKey(PersistentStorage.Key.APPLICATION_LANGUAGE).first()?.let {
        Gson().fromJsonWithTypeToken<LanguageModel>(it)
    } ?: getLanguageList()[0]
}

suspend fun setSelectedLanguage(context: Context, language: LanguageModel) {
    PersistentStorage.getInstance(context)
        .saveKey(PersistentStorage.Key.APPLICATION_LANGUAGE, Gson().toJsonWithTypeToken(language))
}