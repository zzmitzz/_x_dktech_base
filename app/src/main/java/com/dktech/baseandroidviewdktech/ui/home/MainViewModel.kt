package com.dktech.baseandroidviewdktech.ui.home

import android.content.Context
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dktech.baseandroidviewdktech.base.BaseViewModel
import com.dktech.baseandroidviewdktech.data.local.AppDatabase
import com.dktech.baseandroidviewdktech.data.remote.RetrofitClient
import com.dktech.baseandroidviewdktech.ui.home.model.PaintingUIWrapper
import com.dktech.baseandroidviewdktech.utils.helper.cvtFileNameIntoFillSVG
import com.dktech.baseandroidviewdktech.utils.helper.cvtFileNameIntoThumbPNG
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class MainViewModel : ViewModel() {
    private val _dataColorBook = MutableStateFlow<List<PaintingUIWrapper>>(emptyList())
    val dataColorBook = _dataColorBook.asStateFlow()

    var loadingState = MutableStateFlow<Boolean>(false)


    fun loadColorBookData(
        mContext: Context
    ) {
        val localData = AppDatabase.getDatabase(mContext).colorSegmentDAO()

        viewModelScope.launch {
            loadingState.value = true
            val result = RetrofitClient.getColoringBookData()
            val alreadyColoredPainting = localData.getDistinctFileNames().first()
            val data = result.map {
                PaintingUIWrapper(
                    remoteThumb = it.thumbnail ?: "",
                    cacheThumb = if (alreadyColoredPainting.contains(it.fileName)) checkFileExistCache(
                        mContext,
                        cvtFileNameIntoThumbPNG(it.fileName)
                    )?.toUri() else null,
                    fileName = it.fileName ?: "",
                    fillSVG = it.fillFile,
                    strokeSVG = it.strokeFile
                )
            }
            _dataColorBook.value = data
            loadingState.value = false
        }
    }

    private suspend fun checkFileExistCache(mContext: Context, fileName: String?): File? {
        if(fileName == null) return null
        return withContext(Dispatchers.IO) {
            try {
                val file = File(mContext.cacheDir, fileName)
                if (file.exists()) {
                    return@withContext file
                } else {
                    return@withContext null
                }
            } catch (e: Exception) {
                return@withContext null
            }
        }
    }
}
