package com.unirfp.ceropapeleo.web

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.webkit.CookieManager
import android.webkit.URLUtil
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController

private const val MINISTRY_URL =
    "https://sede.mjusticia.gob.es/servidorformularios/formularios?idFormulario=790&lang=es_es"

@Composable
fun MinistryWebViewScreen(
    navController: NavController,
    certificateCode: String,
    onPdfDownloadStarted: (Long) -> Unit
) {
    AndroidView(
        factory = { ctx ->
            WebView(ctx).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.useWideViewPort = true
                settings.loadWithOverviewMode = true

                webViewClient = WebViewClient()

                setDownloadListener { url, userAgent, contentDisposition, mimeType, _ ->
                    Toast.makeText(
                        ctx,
                        "Descarga detectada. Guardando PDF oficial...",
                        Toast.LENGTH_LONG
                    ).show()

                    val downloadId = downloadPdf(
                        context = ctx,
                        downloadUrl = url,
                        userAgent = userAgent,
                        contentDisposition = contentDisposition,
                        mimeType = mimeType
                    )

                    onPdfDownloadStarted(downloadId)
                    navController.navigate("common_form/$certificateCode") {
                        popUpTo("home")
                    }
                }

                loadUrl(MINISTRY_URL)
            }
        }
    )
}

private fun downloadPdf(
    context: Context,
    downloadUrl: String,
    userAgent: String,
    contentDisposition: String?,
    mimeType: String?
): Long {
    val request = DownloadManager.Request(Uri.parse(downloadUrl))

    val cookies = CookieManager.getInstance().getCookie(downloadUrl)
    if (!cookies.isNullOrEmpty()) {
        request.addRequestHeader("Cookie", cookies)
    }

    request.addRequestHeader("User-Agent", userAgent)
    request.addRequestHeader("Referer", MINISTRY_URL)

    request.setNotificationVisibility(
        DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
    )
    request.allowScanningByMediaScanner()

    val fileName = URLUtil.guessFileName(
        downloadUrl,
        contentDisposition,
        mimeType ?: "application/pdf"
    )

    request.setDestinationInExternalPublicDir(
        Environment.DIRECTORY_DOWNLOADS,
        fileName
    )

    val downloadManager =
        context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

    val downloadId = downloadManager.enqueue(request)

    Toast.makeText(
        context,
        "El PDF se está descargando en Descargas",
        Toast.LENGTH_LONG
    ).show()

    return downloadId
}