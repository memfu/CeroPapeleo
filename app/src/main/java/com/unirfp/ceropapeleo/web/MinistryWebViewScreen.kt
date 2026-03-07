package com.unirfp.ceropapeleo.web

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.webkit.CookieManager
import android.webkit.DownloadListener
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// URL configurable del formulario en la Sede Electrónica del Ministerio
const val MINISTRY_URL =
    "https://sede.mjusticia.gob.es/servidorformularios/formularios?idFormulario=790&lang=es_es"

@Composable
fun MinistryWebViewScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            WebView(ctx).apply {

                // CONFIGURACIÓN WEBVIEW
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.useWideViewPort = true
                settings.loadWithOverviewMode = true
                settings.userAgentString = settings.userAgentString // asegura compatibilidad

                webViewClient = WebViewClient()

                // DETECCIÓN DE DESCARGAS
                setDownloadListener { downloadUrl, userAgent, contentDisposition, mimeType, contentLength ->

                    Toast.makeText(
                        context,
                        "Descarga detectada...",
                        Toast.LENGTH_SHORT
                    ).show()

                    downloadFile(
                        context = context,
                        url = downloadUrl,
                        userAgent = userAgent,
                        contentDisposition = contentDisposition,
                        mimeType = mimeType
                    )
                }

                // CARGAR WEB DEL MINISTERIO
                loadUrl(MINISTRY_URL)
            }
        }
    )
}

fun downloadFile(
    context: Context,
    url: String,
    userAgent: String?,
    contentDisposition: String?,
    mimeType: String?
) {

    val cookieManager = CookieManager.getInstance()
    val cookies = cookieManager.getCookie(url) ?: ""

    val request = DownloadManager.Request(Uri.parse(url))

    request.setMimeType(mimeType)
    request.addRequestHeader("cookie", cookies)
    userAgent?.let {
        request.addRequestHeader("User-Agent", it)
    }

    request.setDescription("Descargando PDF oficial...")
    request.setTitle(generateFileName())

    request.allowScanningByMediaScanner()
    request.setNotificationVisibility(
        DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
    )

    request.setDestinationInExternalPublicDir(
        Environment.DIRECTORY_DOWNLOADS,
        generateFileName()
    )

    val downloadManager =
        context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

    downloadManager.enqueue(request)

    Toast.makeText(
        context,
        "Descargando PDF en la carpeta Downloads",
        Toast.LENGTH_LONG
    ).show()
}

// Genera un nombre único para cada descarga
private fun generateFileName(): String {
    val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    return "formulario_790_$timestamp.pdf"
}