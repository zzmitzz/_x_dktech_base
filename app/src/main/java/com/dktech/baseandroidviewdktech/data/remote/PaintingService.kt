package com.dktech.baseandroidviewdktech.data.remote

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET


data class PaintingResponse(
    @SerializedName("status"  ) var status  : Int?            = null,
    @SerializedName("message" ) var message : String?         = null,
    @SerializedName("data"    ) var data    : ArrayList<PaintingData> = arrayListOf()
)

data class PaintingData(
    @SerializedName("id"         ) var id         : Int?    = null,
    @SerializedName("thumbnail"  ) var thumbnail  : String? = null,
    @SerializedName("fileName"   ) var fileName   : String? = null,
    @SerializedName("fillFile"   ) var fillFile   : String? = null,
    @SerializedName("strokeFile" ) var strokeFile : String? = null
)
interface PaintingService {
    @GET("v1/8e1d281f-9c0f-43f3-a700-3823368daf57")
    suspend fun getColorBook(): PaintingResponse
}