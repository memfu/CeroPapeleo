package com.unirfp.ceropapeleo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.unirfp.ceropapeleo.forms.GenerateFormScreen
import com.unirfp.ceropapeleo.ui.theme.CeroPapeleoTheme
import com.unirfp.ceropapeleo.web.MinistryWebViewScreen

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {

            CeroPapeleoTheme {

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {

                    // Controlador de navegación
                    val navController = rememberNavController()

                    // Grafo de navegación
                    NavHost(
                        navController = navController,
                        startDestination = "form"
                    ) {

                        // Pantalla formulario
                        composable("form") {
                            GenerateFormScreen(navController)
                        }

                        // Pantalla WebView
                        composable("webview") {
                            MinistryWebViewScreen()
                        }
                    }
                }
            }
        }
    }
}