package com.unirfp.ceropapeleo.utils

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import okhttp3.ResponseBody
import android.content.ContentValues
import android.provider.MediaStore
import java.io.OutputStream

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
    fun saveApiPdfToDisk(
        context: Context,
        responseBody: ResponseBody,
        fileName: String
    ): String? {
        return try {
            val resolver = context.contentResolver

            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                put(
                    MediaStore.MediaColumns.RELATIVE_PATH,
                    Environment.DIRECTORY_DOWNLOADS
                )
            }

            val uri = resolver.insert(
                MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                contentValues
            ) ?: return null

            val outputStream: OutputStream? = resolver.openOutputStream(uri)

            outputStream.use { output ->
                if (output != null) {
                    responseBody.byteStream().copyTo(output)
                }
            }

            uri.toString()

        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}