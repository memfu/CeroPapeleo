package com.unirfp.ceropapeleo.api

import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface PdfApiService {

    @Multipart
    @POST("fill-pdf")
    suspend fun fillPdf(
        @Part pdfFile: MultipartBody.Part,
        @Part("user_data") userData: RequestBody,
        @Part("signature") signature: RequestBody
    ): Response<ResponseBody>
}