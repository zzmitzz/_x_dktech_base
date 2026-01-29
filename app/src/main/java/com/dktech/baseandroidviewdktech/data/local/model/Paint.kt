package com.dktech.baseandroidviewdktech.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "paint")
data class Paint(
    @PrimaryKey()
    val fileName: String,
    val finishedDate: Long,
)
