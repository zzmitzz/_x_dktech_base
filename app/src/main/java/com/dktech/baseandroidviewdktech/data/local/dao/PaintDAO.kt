package com.dktech.baseandroidviewdktech.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dktech.baseandroidviewdktech.data.local.model.ColoredSegment
import com.dktech.baseandroidviewdktech.data.local.model.Paint
import com.dktech.baseandroidviewdktech.model.Painting
import kotlinx.coroutines.flow.Flow

@Dao
interface PaintDAO {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertFinishedPaint(paint: Paint)

    @Query("SELECT * FROM paint WHERE fileName=:fileName ")
    fun getPaint(fileName: String): Flow<Paint?>

    @Query("DELETE FROM paint WHERE fileName=:fileName")
    suspend fun deletePaint(fileName: String)
}
