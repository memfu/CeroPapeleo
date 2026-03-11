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
import com.unirfp.ceropapeleo.model.GenerateRequest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenerateFormScreen(navController: NavController) {
    var formData by remember { mutableStateOf(GenerateRequest()) }
    val scrollState = rememberScrollState()

    // Flag para controlar cuándo mostrar los errores visuales
    var showErrors by remember { mutableStateOf(false) }

    // --- LÓGICA DE VALIDACIÓN (Criterio MVP) ---
    val isNameValid = formData.applicant.name.isNotBlank()
    val isSurnameValid = formData.applicant.firstSurname.isNotBlank()
    val isDocumentIdValid = formData.applicant.documentId.isNotBlank()
    val isStreetValid = formData.applicant.address.street.isNotBlank()
    val isCityValid = formData.applicant.address.city.isNotBlank()
    val isDeceasedNameValid = formData.deathRelatedDetails.deceased.name.isNotBlank()
    val isDeathDateValid = formData.deathRelatedDetails.deceased.deathDate.isNotBlank()

    val isFormValid = isNameValid && isSurnameValid && isDocumentIdValid &&
            isStreetValid && isCityValid && isDeceasedNameValid && isDeathDateValid

    Scaffold(
        topBar = { TopAppBar(title = { Text("Datos Modelo 790") }) }
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

            // --- SECCIÓN: SOLICITANTE ---
            FormSectionTitle("Datos del Solicitante")

            CustomTextField(
                value = formData.applicant.name,
                onValueChange = { formData = formData.copy(applicant = formData.applicant.copy(name = it)) },
                label = "Nombre",
                isError = showErrors && !isNameValid
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CustomTextField(
                    value = formData.applicant.firstSurname,
                    onValueChange = { formData = formData.copy(applicant = formData.applicant.copy(firstSurname = it)) },
                    label = "1er Apellido",
                    isError = showErrors && !isSurnameValid,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = formData.applicant.secondSurname,
                    onValueChange = { formData = formData.copy(applicant = formData.applicant.copy(secondSurname = it)) },
                    label = { Text("2do Apellido (Opcional)") },
                    modifier = Modifier.weight(1f)
                )
            }

            CustomTextField(
                value = formData.applicant.documentId,
                onValueChange = { formData = formData.copy(applicant = formData.applicant.copy(documentId = it)) },
                label = "DNI/NIE",
                isError = showErrors && !isDocumentIdValid
            )

            // --- SECCIÓN: DIRECCIÓN ---
            FormSectionTitle("Domicilio")

            CustomTextField(
                value = formData.applicant.address.street,
                onValueChange = { formData = formData.copy(applicant = formData.applicant.copy(address = formData.applicant.address.copy(street = it))) },
                label = "Calle/Vía",
                isError = showErrors && !isStreetValid
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = formData.applicant.address.number,
                    onValueChange = { formData = formData.copy(applicant = formData.applicant.copy(address = formData.applicant.address.copy(number = it))) },
                    label = { Text("Nº") },
                    modifier = Modifier.weight(1f)
                )
                CustomTextField(
                    value = formData.applicant.address.city,
                    onValueChange = { formData = formData.copy(applicant = formData.applicant.copy(address = formData.applicant.address.copy(city = it))) },
                    label = "Ciudad",
                    isError = showErrors && !isCityValid,
                    modifier = Modifier.weight(2f)
                )
            }

            // --- SECCIÓN: FALLECIDO ---
            FormSectionTitle("Datos del Fallecido")

            CustomTextField(
                value = formData.deathRelatedDetails.deceased.name,
                onValueChange = {
                    val deceased = formData.deathRelatedDetails.deceased.copy(name = it)
                    formData = formData.copy(deathRelatedDetails = formData.deathRelatedDetails.copy(deceased = deceased))
                },
                label = "Nombre del Causante",
                isError = showErrors && !isDeceasedNameValid
            )

            CustomTextField(
                value = formData.deathRelatedDetails.deceased.deathDate,
                onValueChange = {
                    val deceased = formData.deathRelatedDetails.deceased.copy(deathDate = it)
                    formData = formData.copy(deathRelatedDetails = formData.deathRelatedDetails.copy(deceased = deceased))
                },
                label = "Fecha Defunción (YYYY-MM-DD)",
                isError = showErrors && !isDeathDateValid
            )

            Spacer(modifier = Modifier.height(24.dp))

            // BOTÓN GENERAR CON VALIDACIÓN BLOQUEANTE
            Button(
                onClick = {
                    showErrors = true
                    if (isFormValid) {
                        // Proceder con la lógica de envío al backend
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium
            ) {
                Text("GENERAR PDF RELLENO")
            }

            // El botón de navegación externa no suele requerir validación previa del form
            OutlinedButton(
                onClick = { navController.navigate("webview") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("IR A LA SEDE ELECTRÓNICA (WEB)")
            }
        }
    }
}

/**
 * Componente reutilizable para campos obligatorios con manejo de error
 */
@Composable
fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isError: Boolean,
    modifier: Modifier = Modifier.fillMaxWidth()
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        isError = isError,
        supportingText = {
            if (isError) {
                Text(text = "Este campo es obligatorio", color = MaterialTheme.colorScheme.error)
            }
        },
        modifier = modifier
    )
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