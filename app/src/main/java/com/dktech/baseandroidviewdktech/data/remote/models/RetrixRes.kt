package com.emulator.retro.console.game.data.remote.models

import com.google.gson.annotations.SerializedName

data class RetrixRes(
    @SerializedName("status")
    val status: Boolean,
    @SerializedName("data")
    val data: List<RetrixData>
)