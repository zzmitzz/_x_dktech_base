package com.dktech.baseandroidviewdktech.ui.home

import android.content.Context
import androidx.core.net.toFile
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dktech.baseandroidviewdktech.base.BaseViewModel
import com.dktech.baseandroidviewdktech.data.local.AppDatabase
import com.dktech.baseandroidviewdktech.data.remote.RetrofitClient
import com.dktech.baseandroidviewdktech.ui.home.model.PaintingCategory
import com.dktech.baseandroidviewdktech.ui.home.model.PaintingUIWrapper
import com.dktech.baseandroidviewdktech.utils.helper.cvtFileNameIntoFillSVG
import com.dktech.baseandroidviewdktech.utils.helper.cvtFileNameIntoThumbPNG
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import java.io.File

class MainViewModel : ViewModel() {
    private val _dataColorBook = MutableStateFlow<List<PaintingUIWrapper>>(emptyList())
    val dataColorBook = _dataColorBook.asStateFlow()

    var loadingState = MutableStateFlow<Boolean>(false)

    fun loadColorBookData(mContext: Context) {
        val localData = AppDatabase.getDatabase(mContext).colorSegmentDAO()

        viewModelScope.launch {
            loadingState.value = true
            val result = RetrofitClient.getColoringBookData()
            val alreadyColoredPainting = localData.getDistinctFileNames().first()
            val data =
                result.map {
                    val fileName = it.category + it.fileName
                    val cacheThumb =
                        if (alreadyColoredPainting.contains(fileName)) {
                            checkFileExistCache(
                                mContext,
                                cvtFileNameIntoThumbPNG(fileName),
                            )?.toUri().toString()
                        } else {
                            null
                        }
                    PaintingUIWrapper(
                        remoteThumb = it.thumbnail ?: "",
                        cacheThumb = cacheThumb,
                        fileName = fileName,
                        fillSVG = it.fillFile.first(),
                        strokeSVG = it.strokeFile,
                        lastModifiedCache = cacheThumb?.toUri()?.toFile()?.lastModified() ?: 0L,
                        category = PaintingCategory.entries.find { category -> category.categoryName.equals(it.category, true) },
                    )
                }
            _dataColorBook.value = data
            loadingState.value = false
        }
    }

//    fun updateCategory(
//        mContext: Context,
//        cate: PaintingCategory,
//    ) {
//        currentSelectCategory = cate
//        loadColorBookData(mContext)
//    }

    private suspend fun checkFileExistCache(
        mContext: Context,
        fileName: String?,
    ): File? {
        if (fileName == null) return null
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
