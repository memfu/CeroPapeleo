package com.unirfp.ceropapeleo

import android.app.DownloadManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.webkit.CookieManager
import android.webkit.URLUtil
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.unirfp.ceropapeleo.ui.theme.CeroPapeleoTheme


class MainActivity : ComponentActivity() {

    private lateinit var myWebView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // 3. AQUÍ es donde pones el findViewById
        myWebView = findViewById(R.id.mi_webview_id)

        myWebView.settings.javaScriptEnabled = true
        myWebView.webViewClient = WebViewClient()

// CARGAR LA WEB
        myWebView.loadUrl("https://www.mjusticia.gob.es/es/ciudadania/formulario-790")

// CONFIGURAR LA DESCARGA DIRECTA
        myWebView.setDownloadListener { url, userAgent, contentDisposition, mimetype, contentLength ->
            // 1. Crear la petición de descarga
            val request = DownloadManager.Request(Uri.parse(url))

            // 2. Usar los datos que encontraste (User-Agent y Referer)
            val cookies = CookieManager.getInstance().getCookie(url)
            request.addRequestHeader("cookie", cookies)
            request.addRequestHeader("User-Agent", userAgent)
            request.addRequestHeader("Referer", "https://www.mjusticia.gob.es/")

            // 3. Configurar la notificación y el destino
            request.allowScanningByMediaScanner()
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)

            val fileName = URLUtil.guessFileName(url, contentDisposition, mimetype)
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)

            // 4. Ejecutar la descarga
            val dm = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
            dm.enqueue(request)

            // Opcional: Avisar al usuario
            Toast.makeText(applicationContext, "Descargando formulario...", Toast.LENGTH_LONG).show()
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    CeroPapeleoTheme {
        Greeting("Android")
    }
}