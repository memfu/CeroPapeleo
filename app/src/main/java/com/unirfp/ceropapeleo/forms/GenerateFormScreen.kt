package com.unirfp.ceropapeleo.forms

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
import kotlinx.coroutines.launch

import com.ceropapeleo.ui.forms.*
import com.unirfp.ceropapeleo.api.ApiClient
import com.unirfp.ceropapeleo.utils.DownloadUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenerateFormScreen() {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var formState by remember { mutableStateOf(GenerateRequest()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Nuevo Formulario 790-006") })
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

            Text("Tipo de Certificado", style = MaterialTheme.typography.titleMedium)

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

            if (formState.certificateType != CertificateType.CRIMINAL_RECORD) {

                SectionTitle("Datos del Fallecido")

                val details =
                    formState.deathRelatedDetails ?: DeathRelatedDetails()

                OutlinedTextField(
                    value = details.deceased.name,
                    onValueChange = {

                        val updated = details.copy(
                            deceased = details.deceased.copy(name = it)
                        )

                        formState = formState.copy(
                            deathRelatedDetails = updated
                        )
                    },
                    label = { Text("Nombre fallecido") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = details.deceased.deathDate,
                    onValueChange = {

                        val updated = details.copy(
                            deceased = details.deceased.copy(deathDate = it)
                        )

                        formState = formState.copy(
                            deathRelatedDetails = updated
                        )
                    },
                    label = { Text("Fecha fallecimiento (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (formState.certificateType == CertificateType.LAST_WILL) {

                SectionTitle("Datos Últimas Voluntades")

                val details =
                    formState.deathRelatedDetails ?: DeathRelatedDetails()

                val extra =
                    details.lastWillExtra ?: LastWillExtra()

                OutlinedTextField(
                    value = extra.notary,
                    onValueChange = {

                        val updated = details.copy(
                            lastWillExtra = extra.copy(notary = it)
                        )

                        formState = formState.copy(
                            deathRelatedDetails = updated
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

                                // 1️⃣ Llamada al backend
                                val response =
                                    ApiClient.apiService.generatePdf(formState)

                                if (response.status == "SUCCESS") {

                                    // 2️⃣ Descargar PDF
                                    val url = "https://sede.mjusticia.gob.es/form790/${response.requestId}"
                                    navController.navigate("webview/$url")

                                    errorMessage =
                                        "PDF generado. Descarga iniciada."

                                } else {

                                    errorMessage =
                                        "Error servidor: ${response.message}"
                                }

                            } catch (e: Exception) {

                                errorMessage =
                                    "Error de conexión: ${e.localizedMessage}"

                            } finally {

                                isLoading = false
                            }
                        }

                    } else {

                        errorMessage =
                            "Por favor rellena DNI y nombre"
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                enabled = !isLoading
            ) {

                if (isLoading) {

                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp)
                    )

                } else {

                    Text("GENERAR Y DESCARGAR PDF")
                }
            }

            errorMessage?.let {
                Text(
                    it,
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
        fontWeight = FontWeight.Bold
    )
}

@Composable
fun CertificateTypeSelector(
    selected: CertificateType,
    onSelected: (CertificateType) -> Unit
) {
    Column {

        CertificateType.values().forEach { type ->

            Row(verticalAlignment = Alignment.CenterVertically) {

                RadioButton(
                    selected = type == selected,
                    onClick = { onSelected(type) }
                )

                Text(type.name.replace("_", " "))
            }
        }
    }
}

fun validateForm(req: GenerateRequest): Boolean {

    return req.applicant.documentId.isNotBlank()
            && req.applicant.name.isNotBlank()
}