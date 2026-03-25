package com.unirfp.ceropapeleo.home // Esto debe coincidir con la carpeta nueva

import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.unirfp.ceropapeleo.forms.GenerateFormViewModel

@Composable
fun HomeSelectionScreen(
    navController: NavController,
    viewModel: GenerateFormViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Título Principal
        Text(
            text = "Asistencia para rellenar el formulario 790",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 34.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Subtítulo
        Text(
            text = "Seleccione qué certificado quiere solicitar:",
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(48.dp))

        // --- BOTÓN 1: ANTECEDENTES PENALES ---
        OutlinedButton(
            onClick = {
                viewModel.startNewForm("17")
                navController.navigate("webview/17")
            },
            modifier = Modifier.fillMaxWidth().height(80.dp),
            shape = MaterialTheme.shapes.medium
        ) {
            Text("Certificado de antecedentes penales", fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- BOTÓN 2: ÚLTIMAS VOLUNTADES ---
        OutlinedButton(
            onClick = {
                viewModel.startNewForm("18")
                navController.navigate("webview/18")
            },
            modifier = Modifier.fillMaxWidth().height(80.dp),
            shape = MaterialTheme.shapes.medium
        ) {
            Text("Certificado de últimas voluntades", fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- BOTÓN 3: COBERTURA FALLECIMIENTO ---
        OutlinedButton(
            onClick = {
                viewModel.startNewForm("19")
                navController.navigate("webview/19")
            },
            modifier = Modifier.fillMaxWidth().height(80.dp),
            shape = MaterialTheme.shapes.medium
        ){
            Text("Certificado de cobertura de fallecimiento", fontSize = 16.sp)
        }
    }
}
