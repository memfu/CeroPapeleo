package com.unirfp.ceropapeleo.api

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {
    // IMPORTANTE: Mantener el "http://" y la barra "/" al final
    // private const val BASE_URL = "http://ceropapeleo-app-env.eba-xcc72vsf.eu-north-1.elasticbeanstalk.com/"
    private const val BASE_URL = "http://10.0.2.2:8080/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val pdfApiService: PdfApiService = retrofit.create(PdfApiService::class.java)
}