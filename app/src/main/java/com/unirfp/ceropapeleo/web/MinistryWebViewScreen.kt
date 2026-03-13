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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView

private const val MINISTRY_URL = "https://sede.mjusticia.gob.es/servidorformularios/formularios?idFormulario=790&lang=es_es"

@Composable
fun MinistryWebViewScreen() {
    val context = LocalContext.current

    AndroidView(
        factory = { ctx ->
            WebView(ctx).apply {
                // Configuración mínima necesaria para sedes electrónicas modernas
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.useWideViewPort = true
                settings.loadWithOverviewMode = true

                // Evita que los enlaces se abran en el navegador externo
                webViewClient = WebViewClient()

                // Detecta la descarga del PDF oficial
                setDownloadListener { url, userAgent, contentDisposition, mimeType, _ ->
                    Toast.makeText(
                        ctx,
                        "Descarga detectada. Guardando PDF oficial...",
                        Toast.LENGTH_LONG
                    ).show()

                    downloadPdf(
                        context = ctx,
                        downloadUrl = url,
                        userAgent = userAgent,
                        contentDisposition = contentDisposition,
                        mimeType = mimeType
                    )
                }

                // Carga la sede electrónica
                loadUrl(MINISTRY_URL)
            }
        }
    )
}

/**
 * Descarga el PDF usando DownloadManager.
 *
 * Se pasan las cookies de sesión y el user-agent de la WebView para conservar
 * el contexto de navegación del usuario en la sede electrónica.
 */
private fun downloadPdf(
    context: Context,
    downloadUrl: String,
    userAgent: String,
    contentDisposition: String?,
    mimeType: String?
) {
    val request = DownloadManager.Request(Uri.parse(downloadUrl))

    // Recuperar cookies activas de la sesión
    val cookies = CookieManager.getInstance().getCookie(downloadUrl)
    if (!cookies.isNullOrEmpty()) {
        request.addRequestHeader("Cookie", cookies)
    }

    // Pasar el user-agent de la WebView
    request.addRequestHeader("User-Agent", userAgent)

    // Referer útil para algunas sedes electrónicas
    request.addRequestHeader("Referer", MINISTRY_URL)

    // Configuración de visibilidad y guardado
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

    downloadManager.enqueue(request)

    Toast.makeText(
        context,
        "El PDF se está descargando en Descargas",
        Toast.LENGTH_LONG
    ).show()
}