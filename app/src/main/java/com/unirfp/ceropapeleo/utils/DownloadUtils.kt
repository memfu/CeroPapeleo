package com.unirfp.ceropapeleo.utils

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import okhttp3.ResponseBody
import java.io.File
import java.io.FileOutputStream

object DownloadUtils {

    fun downloadPdf(context: Context, requestId: String) {

        val url = "http://10.0.2.2:8080/download/$requestId"

        val request = DownloadManager.Request(Uri.parse(url))
            .setTitle("Formulario 790-006")
            .setDescription("Descargando certificado generado...")
            .setNotificationVisibility(
                DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
            )
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)
            .setDestinationInExternalPublicDir(
                Environment.DIRECTORY_DOWNLOADS,
                "certificado_$requestId.pdf"
            )

        val downloadManager =
            context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

        downloadManager.enqueue(request)
    }

    // FUNCION Para el PDF que devuelve Retrofit tras el POST /fill-pdf)
    fun saveApiPdfToDisk(context: Context, body: ResponseBody, fileName: String): File? {
        return try {
            // Usamos carpeta de la App para evitar problemas de permisos en Android 13+
            val destinationFile = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), fileName)

            body.byteStream().use { input ->
                FileOutputStream(destinationFile).use { output ->
                    input.copyTo(output)
                }
            }
            destinationFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}