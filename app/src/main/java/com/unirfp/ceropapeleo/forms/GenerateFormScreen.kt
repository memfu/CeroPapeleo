package com.unirfp.ceropapeleo.forms

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import com.unirfp.ceropapeleo.api.ApiClient
import com.unirfp.ceropapeleo.ui.forms.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenerateFormScreen(navController: NavController) {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Estado del formulario
    var formState by remember { mutableStateOf(GenerateRequest()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nuevo Formulario 790-006") }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            Text(
                "Tipo de Certificado",
                style = MaterialTheme.typography.titleMedium
            )

            CertificateTypeSelector(
                selected = formState.certificateType,
                onSelected = {
                    formState = formState.copy(certificateType = it)
                }
            )

            HorizontalDivider()

            SectionTitle("Datos del Solicitante")

            OutlinedTextField(
                value = formState.applicant.documentId,
                onValueChange = {
                    formState = formState.copy(
                        applicant = formState.applicant.copy(documentId = it)
                    )
                },
                label = { Text("DNI/NIE") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = formState.applicant.name,
                onValueChange = {
                    formState = formState.copy(
                        applicant = formState.applicant.copy(name = it)
                    )
                },
                label = { Text("Nombre") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = formState.applicant.firstSurname,
                onValueChange = {
                    formState = formState.copy(
                        applicant = formState.applicant.copy(firstSurname = it)
                    )
                },
                label = { Text("Primer Apellido") },
                modifier = Modifier.fillMaxWidth()
            )

            // Lógica Condicional: Datos del fallecido
            if (formState.certificateType != CertificateType.CRIMINAL_RECORD) {

                SectionTitle("Datos del Fallecido")

                val details = formState.deathRelatedDetails ?: DeathRelatedDetails()

                OutlinedTextField(
                    value = details.deceased.name,
                    onValueChange = { newValue ->
                        formState = formState.copy(
                            deathRelatedDetails = details.copy(
                                deceased = details.deceased.copy(name = newValue)
                            )
                        )
                    },
                    label = { Text("Nombre fallecido") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = details.deathDate,
                    onValueChange = { newValue ->
                        formState = formState.copy(
                            deathRelatedDetails = details.copy(deathDate = newValue)
                        )
                    },
                    label = { Text("Fecha fallecimiento (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Lógica Condicional: Últimas Voluntades
            if (formState.certificateType == CertificateType.LAST_WILL) {

                SectionTitle("Datos Últimas Voluntades")

                val extra = formState.lastWillExtra ?: LastWillExtra()

                OutlinedTextField(
                    value = extra.notary,
                    onValueChange = { newValue ->
                        formState = formState.copy(
                            lastWillExtra = extra.copy(notary = newValue)
                        )
                    },
                    label = { Text("Notario") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Button(
                onClick = {
                    if (validateForm(formState)) {
                        isLoading = true
                        errorMessage = null

                        scope.launch {
                            try {
                                val response = ApiClient.apiService.generatePdf(formState)

                                if (response.status == "SUCCESS") {
                                    // Se navega a la ruta webview
                                    val url = "https://sede.mjusticia.gob.es/form790/${response.requestId}"
                                    navController.navigate("webview/$url")
                                } else {
                                    errorMessage = "Error servidor: ${response.message}"
                                }
                            } catch (e: Exception) {
                                errorMessage = "Error de conexión: ${e.localizedMessage}"
                            } finally {
                                isLoading = false
                            }
                        }
                    } else {
                        errorMessage = "Por favor rellena DNI y nombre"
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    Text("GENERAR Y DESCARGAR PDF")
                }
            }

            errorMessage?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 8.dp)
    )
}

@Composable
fun CertificateTypeSelector(
    selected: CertificateType,
    onSelected: (CertificateType) -> Unit
) {
    Column {
        CertificateType.entries.forEach { type ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                RadioButton(
                    selected = type == selected,
                    onClick = { onSelected(type) }
                )
                Text(
                    text = type.name.replace("_", " "),
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}

fun validateForm(req: GenerateRequest): Boolean {
    return req.applicant.documentId.isNotBlank() && req.applicant.name.isNotBlank()
}