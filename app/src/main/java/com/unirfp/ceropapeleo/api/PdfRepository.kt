package com.unirfp.ceropapeleo.api

import com.google.gson.Gson
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import java.io.File

class PdfRepository(private val apiService: PdfApiService = ApiClient.pdfApiService) {

    suspend fun uploadAndFillPdf(file: File, userDataMap: Map<String, String>): ResponseBody? {
        try {
            // 1. Preparar el archivo PDF (Forma compatible universal)
            val mediaTypePdf = MediaType.parse("application/pdf")
            val requestFile = RequestBody.create(mediaTypePdf, file)
            val pdfPart = MultipartBody.Part.createFormData("pdf_file", file.name, requestFile)

            // 2. Preparar el JSON (Forma compatible universal)
            val jsonString = Gson().toJson(userDataMap)
            val mediaTypeJson = MediaType.parse("application/json")
            val userDataPart = RequestBody.create(mediaTypeJson, jsonString)

            // 3. Llamada
            val response = apiService.fillPdf(pdfPart, userDataPart)

            if (!response.isSuccessful) {
                println("ERROR BACKEND: ${response.code()} - ${response.errorBody()?.string()}")
            }

            return response.body()
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
}