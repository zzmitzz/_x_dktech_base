package com.dktech.baseandroidviewdktech.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dktech.baseandroidviewdktech.data.local.model.ColoredSegment
import kotlinx.coroutines.flow.Flow

@Dao
interface ColoredSegmentDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertColoredSegment(segment: ColoredSegment)

    @Query("SELECT * FROM color_segment WHERE svg_file_name=:fileName ")
    fun getColoredSegmentByFileName(fileName: String): Flow<List<ColoredSegment>>

    @Query("SELECT DISTINCT svg_file_name FROM color_segment")
    fun getDistinctFileNames(): Flow<List<String>>
}