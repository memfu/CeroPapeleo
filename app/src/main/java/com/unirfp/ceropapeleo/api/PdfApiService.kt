package com.unirfp.ceropapeleo.api

import com.unirfp.ceropapeleo.forms.GenerateRequest
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

data class GenerateResponse(
    val status: String,
    val requestId: String,
    val generatedAt: String,
    val message: String
)

interface ApiService {

    @POST("generate")
    suspend fun generatePdf(
        @Body request: GenerateRequest
    ): GenerateResponse
}

object ApiClient {

    // CAMBIAR por la URL real del backend de Marilú
    private const val BASE_URL = "http://10.0.2.2:8080/"

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}