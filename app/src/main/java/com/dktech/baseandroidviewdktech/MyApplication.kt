package com.dktech.baseandroidviewdktech

import android.app.Application
import android.content.Context
import android.os.StrictMode
import androidx.datastore.preferences.preferencesDataStore
import com.dktech.baseandroidviewdktech.utils.PersistentStorage


val Context.appDataStore by preferencesDataStore(
    name = PersistentStorage.Key.PREFERENCE_KEY
)

class MyApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        if(BuildConfig.BUILD_TYPE == "debug"){
            StrictMode.ThreadPolicy.Builder().detectAll().penaltyLog().build()
        }
    }
}