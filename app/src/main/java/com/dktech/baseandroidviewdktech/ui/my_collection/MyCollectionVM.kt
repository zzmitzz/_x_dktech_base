package com.dktech.baseandroidviewdktech.ui.my_collection

import android.content.Context
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dktech.baseandroidviewdktech.data.local.AppDatabase
import com.dktech.baseandroidviewdktech.data.local.dao.ColoredSegmentDAO
import com.dktech.baseandroidviewdktech.model.Painting
import com.dktech.baseandroidviewdktech.ui.home.model.PaintingUIWrapper
import com.dktech.baseandroidviewdktech.utils.helper.cvtFileNameIntoFillSVG
import com.dktech.baseandroidviewdktech.utils.helper.cvtFileNameIntoStrokeSVG
import com.dktech.baseandroidviewdktech.utils.helper.cvtFileNameIntoThumbPNG
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File

class MyCollectionVM : ViewModel() {
    private lateinit var coloredSegmentDAO: ColoredSegmentDAO
    val listData: MutableStateFlow<List<PaintingUIWrapper>> = MutableStateFlow<List<PaintingUIWrapper>>(emptyList())

    fun loadData(mContext: Context) {
        coloredSegmentDAO = AppDatabase.getDatabase(mContext).colorSegmentDAO()
        viewModelScope.launch(Dispatchers.IO) {
            coloredSegmentDAO.getDistinctFileNames().collect {
                val distinctFile =
                    it.distinct().map { nameFile ->
                        PaintingUIWrapper(
                            fileName = nameFile,
                            fillSVG = cvtFileNameIntoFillSVG(nameFile),
                            strokeSVG = cvtFileNameIntoStrokeSVG(nameFile),
                            cacheThumb = File(mContext.cacheDir, cvtFileNameIntoThumbPNG(nameFile)).toUri().toString(),
                            remoteThumb = null,
                        )
                    }
                listData.value = distinctFile
            }
        }
    }

    suspend fun deleteColoredSegments(fileName: String) {
        coloredSegmentDAO.deleteColoredSegmentsByFileName(fileName)
    }
}
