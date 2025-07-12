package com.dktech.baseandroidviewdktech.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.dktech.baseandroidviewdktech.MainViewModel

class ViewModelFactory() : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel() as T
        }
        throw IllegalArgumentException("Unknown class name")
    }
}