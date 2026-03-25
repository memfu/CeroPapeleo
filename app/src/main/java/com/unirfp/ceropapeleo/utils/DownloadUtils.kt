package com.unirfp.ceropapeleo.utils

import android.app.DownloadManager
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import okhttp3.ResponseBody
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

    fun resolveDownloadedFilePath(
        context: Context,
        downloadId: Long
    ): String? {
        val downloadManager =
            context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

        val query = DownloadManager.Query().setFilterById(downloadId)
        val cursor = downloadManager.query(query)

        cursor.use {
            if (it != null && it.moveToFirst()) {
                val statusIndex = it.getColumnIndex(DownloadManager.COLUMN_STATUS)
                val uriIndex = it.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)

                if (statusIndex != -1 && uriIndex != -1) {
                    val status = it.getInt(statusIndex)
                    val localUri = it.getString(uriIndex)

                    if (status == DownloadManager.STATUS_SUCCESSFUL && !localUri.isNullOrBlank()) {
                        val uri = Uri.parse(localUri)
                        return uri.path
                    }
                }
            }
        }

        return null
    }

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