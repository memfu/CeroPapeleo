package com.unirfp.ceropapeleo.api

import android.util.Log
import com.google.gson.Gson
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import java.io.File

class PdfRepository(private val apiService: PdfApiService = ApiClient.pdfApiService) {
    suspend fun uploadAndFillPdf(file: File, userDataMap: Map<String, String>): ResponseBody? {
        return try {
            val mediaTypePdf = MediaType.parse("application/pdf")
            val requestFile = RequestBody.create(mediaTypePdf, file)
            val pdfPart = MultipartBody.Part.createFormData("pdf_file", file.name, requestFile)

            val jsonString = Gson().toJson(userDataMap)
            val mediaTypeJson = MediaType.parse("application/json")
            val userDataPart = RequestBody.create(mediaTypeJson, jsonString)

            Log.d("PDF_API", "URL llamada: fill-pdf")
            Log.d("PDF_API", "Archivo enviado: ${file.absolutePath}")
            Log.d("PDF_API", "Existe archivo: ${file.exists()} | tamaño=${file.length()}")
            Log.d("PDF_API", "JSON enviado: $jsonString")

            val response = apiService.fillPdf(pdfPart, userDataPart)

            Log.d("PDF_API", "HTTP code: ${response.code()}")
            Log.d("PDF_API", "HTTP message: ${response.message()}")

            if (response.isSuccessful) {
                response.body()
            } else {
                Log.e("PDF_API", "Error body: ${response.errorBody()?.string()}")
                null
            }
        } catch (e: Exception) {
            Log.e("PDF_API", "Excepción en uploadAndFillPdf", e)
            null
        }
    }
    /*suspend fun uploadAndFillPdf(file: File, userDataMap: Map<String, String>): ResponseBody? {
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
    }*/
}