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
// --- NUEVA IMPORTACIÓN ---
import com.unirfp.ceropapeleo.home.HomeSelectionScreen
import com.unirfp.ceropapeleo.forms.GenerateFormScreen
import com.unirfp.ceropapeleo.ui.theme.CeroPapeleoTheme
import com.unirfp.ceropapeleo.web.MinistryWebViewScreen
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.unirfp.ceropapeleo.forms.CertificateDetailsScreen

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
                    val navController = rememberNavController()

                    NavHost(
                        navController = navController,
                        startDestination = "home"
                    ) {
                        // Pantalla de selección inicial
                        composable("home") {
                            HomeSelectionScreen(navController)
                        }

                        // Pantalla del formulario reutilizable para 17, 18 y 19
                        composable(
                            route = "generate_form/{certificateCode}",
                            arguments = listOf(
                                navArgument("certificateCode") {
                                    type = NavType.StringType
                                }
                            )
                        ) { backStackEntry ->
                            val certificateCode =
                                backStackEntry.arguments?.getString("certificateCode").orEmpty()

                            GenerateFormScreen(
                                navController = navController,
                                certificateCode = certificateCode
                            )
                        }

                        // Pantalla del WebView
                        composable("webview") {
                            MinistryWebViewScreen()
                        }

                        composable("certificate_details") {
                            CertificateDetailsScreen(navController = navController)
                        }
                    }
                }
            }
        }
    }
}