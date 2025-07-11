package com.dktech.baseandroidviewdktech.base

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

abstract class BaseViewModel: ViewModel(){
    private val _loadingDialog = MutableStateFlow<Boolean>(false)
    val loadingDialog = _loadingDialog.asStateFlow()

    fun setLoadingDialog(isShow: Boolean){
        _loadingDialog.value = isShow
    }
}