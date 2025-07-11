package com.dktech.baseandroidviewdktech

import android.app.Application
import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import com.dktech.baseandroidviewdktech.utils.PersistentStorage


val Context.appDataStore by preferencesDataStore(
    name = PersistentStorage.Key.PREFERENCE_KEY
)

class MyApplication: Application() {

}