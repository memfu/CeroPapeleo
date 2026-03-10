package com.unirfp.ceropapeleo.forms

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
// IMPORTANTE: Verifica que esta ruta de importación sea la correcta en tu proyecto
import com.unirfp.ceropapeleo.model.GenerateRequest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenerateFormScreen(navController: NavController) {
    // Estado del formulario
    var formData by remember { mutableStateOf(GenerateRequest()) }
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Datos Modelo 790") })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Complete la información para rellenar el PDF oficial", fontSize = 14.sp)

            // --- SECCIÓN: DATOS DEL SOLICITANTE ---
            FormSectionTitle("Datos del Solicitante")

            OutlinedTextField(
                value = formData.applicant.name,
                onValueChange = { formData = formData.copy(applicant = formData.applicant.copy(name = it)) },
                label = { Text("Nombre") },
                modifier = Modifier.fillMaxWidth()
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = formData.applicant.firstSurname,
                    onValueChange = { formData = formData.copy(applicant = formData.applicant.copy(firstSurname = it)) },
                    label = { Text("1er Apellido") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = formData.applicant.secondSurname,
                    onValueChange = { formData = formData.copy(applicant = formData.applicant.copy(secondSurname = it)) },
                    label = { Text("2do Apellido") },
                    modifier = Modifier.weight(1f)
                )
            }

            OutlinedTextField(
                value = formData.applicant.documentId,
                onValueChange = { formData = formData.copy(applicant = formData.applicant.copy(documentId = it)) },
                label = { Text("DNI/NIE") },
                modifier = Modifier.fillMaxWidth()
            )

            // --- SECCIÓN: DIRECCIÓN ---
            FormSectionTitle("Domicilio")

            OutlinedTextField(
                value = formData.applicant.address.street,
                onValueChange = { formData = formData.copy(applicant = formData.applicant.copy(address = formData.applicant.address.copy(street = it))) },
                label = { Text("Calle/Vía") },
                modifier = Modifier.fillMaxWidth()
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = formData.applicant.address.number,
                    onValueChange = { formData = formData.copy(applicant = formData.applicant.copy(address = formData.applicant.address.copy(number = it))) },
                    label = { Text("Nº") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = formData.applicant.address.city,
                    onValueChange = { formData = formData.copy(applicant = formData.applicant.copy(address = formData.applicant.address.copy(city = it))) },
                    label = { Text("Ciudad") },
                    modifier = Modifier.weight(2f)
                )
            }

            // --- SECCIÓN: DATOS DEL FALLECIDO ---
            FormSectionTitle("Datos del Fallecido")

            OutlinedTextField(
                value = formData.deathRelatedDetails.deceased.name,
                onValueChange = {
                    val deceased = formData.deathRelatedDetails.deceased.copy(name = it)
                    formData = formData.copy(deathRelatedDetails = formData.deathRelatedDetails.copy(deceased = deceased))
                },
                label = { Text("Nombre del Causante") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = formData.deathRelatedDetails.deceased.deathDate,
                onValueChange = {
                    val deceased = formData.deathRelatedDetails.deceased.copy(deathDate = it)
                    formData = formData.copy(deathRelatedDetails = formData.deathRelatedDetails.copy(deceased = deceased))
                },
                label = { Text("Fecha Defunción (YYYY-MM-DD)") },
                modifier = Modifier.fillMaxWidth()
            )

            // --- SECCIÓN: ÚLTIMAS VOLUNTADES ---
            FormSectionTitle("Datos Notariales")

            OutlinedTextField(
                value = formData.deathRelatedDetails.lastWillExtra.notary,
                onValueChange = {
                    val extra = formData.deathRelatedDetails.lastWillExtra.copy(notary = it)
                    formData = formData.copy(deathRelatedDetails = formData.deathRelatedDetails.copy(lastWillExtra = extra))
                },
                label = { Text("Notario") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // BOTÓN GENERAR
            Button(
                onClick = { /* TODO: Lógica Backend */ },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium
            ) {
                Text("GENERAR PDF RELLENO")
            }

            // BOTÓN PARA IR AL WEBVIEW
            OutlinedButton(
                onClick = { navController.navigate("webview") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("IR A LA SEDE ELECTRÓNICA (WEB)")
            }
        }
    }
}

@Composable
fun FormSectionTitle(title: String) {
    Text(
        text = title,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 8.dp)
    )
}