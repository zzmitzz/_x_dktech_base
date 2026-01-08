package com.emulator.retro.console.game.data.remote.models

import com.google.gson.annotations.SerializedName

data class RetrixData(
    @SerializedName("id")
    val id: Long,
    @SerializedName("title")
    val title: String,
    @SerializedName("link")
    val link: String,
    @SerializedName("download_link")
    val downloadLink: String,
    @SerializedName("date")
    val date: String,
    @SerializedName("thumbnail")
    val thumb: String,
    @SerializedName("platform")
    val platform: String,
    @SerializedName("region")
    val region: String,
    @SerializedName("version")
    val version: String
)