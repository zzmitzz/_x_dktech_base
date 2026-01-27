package com.dktech.baseandroidviewdktech.data.remote

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET

data class PaintingResponse(
    @SerializedName("status") var status: Int? = null,
    @SerializedName("message") var message: String? = null,
    @SerializedName("data") var data: ArrayList<PaintingData> = arrayListOf(),
)

data class PaintingData(
    @SerializedName("id") var id: Int? = null,
    @SerializedName("thump_url") var thumbnail: String? = null,
    @SerializedName("name") var fileName: String? = null,
    @SerializedName("image_url") var fillFile: List<String> = emptyList(),
    @SerializedName("category") var category: String = "",
    @SerializedName("thump_filter") var strokeFile: String? = null,
)

interface PaintingService {
    @GET("api/ardraw/com.coloring.book.color.asmr.painting.relax?sign=a7f3d9b2c5e8g1h6i4j0k7l3m9n2o5p8q1r6s4t0u")
    suspend fun getColorBook(): PaintingResponse
}
