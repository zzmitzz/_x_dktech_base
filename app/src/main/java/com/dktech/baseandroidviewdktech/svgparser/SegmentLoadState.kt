package com.dktech.baseandroidviewdktech.svgparser

import android.content.Context
import com.dktech.baseandroidviewdktech.data.local.AppDatabase
import com.dktech.baseandroidviewdktech.data.local.dao.ColoredSegmentDAO
import com.dktech.baseandroidviewdktech.model.SegmentUIState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class SegmentLoadState constructor(
    val segmentColoredStateDB: ColoredSegmentDAO,
) {
    suspend fun constructStateSegment(
        fileName: String,
        rawData: List<SegmentUIState>,
    ): List<SegmentUIState> {
        return withContext(Dispatchers.IO) {
            val data =
                segmentColoredStateDB
                    .getColoredSegmentByFileName(fileName)
                    .first()
                    .map { it.segmentId }
            val newData = rawData.toMutableList()
            newData.forEach {
                it.isColored = data.contains(it.id)
            }
            return@withContext newData
        }
    }
}
