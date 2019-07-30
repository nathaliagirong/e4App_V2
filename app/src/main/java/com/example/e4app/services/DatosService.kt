package com.example.e4app.services

import com.example.e4app.models.Data
import com.example.e4app.models.Datos
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface DatosService {

    @GET("getResult")
    fun getDatos(@Query("dateId") dateId: String): Call<Datos>

    @POST("uploadData")
    fun uploadData(@Body newData: Data): Call<Datos>

}