package com.unirfp.ceropapeleo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
// Importamos tu nueva pantalla (Asegúrate de que la ruta sea esta)
import com.unirfp.ceropapeleo.forms.GenerateFormScreen
import com.unirfp.ceropapeleo.ui.theme.CeroPapeleoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Habilita que el diseño use toda la pantalla (incluyendo barra de estado)
        enableEdgeToEdge()

        // setContent es el nuevo motor de tu app (reemplaza a setContentView)
        setContent {
            CeroPapeleoTheme {
                // Surface es el "lienzo" donde se dibuja la app
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // LLAMADA A TU FORMULARIO GENERADO POR IA
                    GenerateFormScreen()
                }
            }
        }
    }
}