package com.unirfp.ceropapeleo.utils

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment

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
}