package com.unirfp.ceropapeleo.forms

import androidx.compose.animation.core.copy
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.ceropapeleo.ui.forms.CertificateType
import com.ceropapeleo.ui.forms.DeathRelatedDetails
import com.ceropapeleo.ui.forms.GenerateRequest
import com.ceropapeleo.ui.forms.LastWillExtra

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenerateFormScreen() {
    // Estado principal del formulario
    var formState by remember { mutableStateOf(GenerateRequest()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val scrollState = rememberScrollState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Nuevo Formulario 790-006") }) }
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

            // Selector de Tipo de Certificado
            CertificateTypeSelector(
                selected = formState.certificateType,
                onSelected = { formState = formState.copy(certificateType = it) }
            )

            HorizontalDivider()

            // Datos del Solicitante (Común para todos)
            SectionTitle("Datos del Solicitante")
            OutlinedTextField(
                value = formState.applicant.documentId,
                onValueChange = {
                    formState =
                        formState.copy(applicant = formState.applicant.copy(documentId = it))
                },
                label = { Text("DNI/NIE") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = formState.applicant.name,
                onValueChange = {
                    formState = formState.copy(applicant = formState.applicant.copy(name = it))
                },
                label = { Text("Nombre") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = formState.applicant.firstSurname,
                onValueChange = {
                    formState =
                        formState.copy(applicant = formState.applicant.copy(firstSurname = it))
                },
                label = { Text("Primer Apellido") },
                modifier = Modifier.fillMaxWidth()
            )

            // LÓGICA CONDICIONAL: Detalles de Fallecimiento
            // Se muestra si es ÚLTIMAS VOLUNTADES o SEGUROS DE VIDA
            if (formState.certificateType != CertificateType.CRIMINAL_RECORD) {
                SectionTitle("Datos del Fallecido")
                val deceasedDetails = formState.deathRelatedDetails ?: DeathRelatedDetails()

                OutlinedTextField(
                    value = deceasedDetails.deceased.name,
                    onValueChange = {
                        formState = formState.copy(
                            deathRelatedDetails = deceasedDetails.copy(
                                deceased = deceasedDetails.deceased.copy(name = it)
                            )
                        )
                    },
                    label = { Text("Nombre del Fallecido") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = deceasedDetails.deathDate,
                    onValueChange = {
                        formState =
                            formState.copy(deathRelatedDetails = deceasedDetails.copy(deathDate = it))
                    },
                    label = { Text("Fecha Fallecimiento (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // LÓGICA CONDICIONAL: Detalles Extra de Últimas Voluntades
            if (formState.certificateType == CertificateType.LAST_WILL) {
                SectionTitle("Datos Últimas Voluntades")
                val extra = formState.lastWillExtra ?: LastWillExtra()
                OutlinedTextField(
                    value = extra.notary,
                    onValueChange = {
                        formState = formState.copy(lastWillExtra = extra.copy(notary = it))
                    },
                    label = { Text("Notario") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            SectionTitle("Pago")
            OutlinedTextField(
                value = formState.payment.amountEur,
                onValueChange = {
                    formState = formState.copy(payment = formState.payment.copy(amountEur = it))
                },
                label = { Text("Importe (€)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )

            // Botón de Acción
            if (errorMessage != null) {
                Text(errorMessage!!, color = MaterialTheme.colorScheme.error)
            }

            Button(
                onClick = {
                    if (validateForm(formState)) {
                        isLoading = true
                        errorMessage = null
                        // TODO: Aquí se conectará la llamada a API POST /generate
                    } else {
                        errorMessage = "Por favor, rellena los campos obligatorios."
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("GENERAR PDF")
                }
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
fun CertificateTypeSelector(selected: CertificateType, onSelected: (CertificateType) -> Unit) {
    Column {
        CertificateType.values().forEach { type ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = (type == selected), onClick = { onSelected(type) })
                Text(type.name.replace("_", " "))
            }
        }
    }
}

fun validateForm(req: GenerateRequest): Boolean {
    // Validación mínima: DNI y Nombre del solicitante no vacíos
    return req.applicant.documentId.isNotBlank() && req.applicant.name.isNotBlank()
}