package com.dktech.baseandroidviewdktech.data.local.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "color_segment")
data class ColoredSegment(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo("svg_file_name")
    val fileName: String,

    @ColumnInfo("segment_id")
    val segmentId: Int,


)