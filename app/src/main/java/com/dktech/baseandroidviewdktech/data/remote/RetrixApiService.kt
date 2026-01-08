package com.emulator.retro.console.game.data.remote

import com.emulator.retro.console.game.data.remote.models.RetrixRes
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface RetrixApiService {

    // FIXME: 10/11/2025 Too many data, optimize me!
    @GET("gba-backup-games")
    fun getAll(@Query("sign") sign: String = "9xvcwstiquvhyxxumj0s"): Call<RetrixRes>

}