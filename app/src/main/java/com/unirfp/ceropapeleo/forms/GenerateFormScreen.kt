package com.unirfp.ceropapeleo.forms

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.unirfp.ceropapeleo.model.GenerateRequest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenerateFormScreen(navController: NavController) {
    var formData by remember { mutableStateOf(GenerateRequest()) }
    val scrollState = rememberScrollState()
    var showErrors by remember { mutableStateOf(false) }

    // --- HELPERS DE VALIDACIÓN ---
    fun String.isOnlyLetters() = this.all { it.isLetter() || it.isWhitespace() }

    // Regex para formato Español DD/MM/AAAA
    fun String.isValidSpanishDate(): Boolean {
        val regex = Regex("^(0[1-9]|[12][0-9]|3[01])/(0[1-9]|1[0-2])/\\d{4}$")
        return regex.matches(this)
    }

    // --- LÓGICA DE VALIDACIÓN GLOBAL ---
    val isFormValid = formData.applicant.name.isNotBlank() &&
            formData.applicant.firstSurname.isNotBlank() &&
            formData.applicant.address.city.isNotBlank() &&
            formData.applicant.address.province.isNotBlank() &&
            formData.applicant.address.country.isNotBlank() &&
            formData.deathRelatedDetails.deceased.name.isNotBlank() &&
            formData.deathRelatedDetails.deceased.firstSurname.isNotBlank() &&
            formData.deathRelatedDetails.deceased.deathDate.isValidSpanishDate()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Formulario 790 - Últimas Voluntades") }) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .drawScrollbar(scrollState)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Completa los campos para generar el PDF de Actos de Última Voluntad listo para entregar", fontSize = 14.sp)

            // --- 1. DATOS DEL SOLICITANTE ---
            FormSectionTitle("1. Datos del Solicitante")

            CustomTextField(
                value = formData.applicant.name,
                onValueChange = { if (it.isOnlyLetters()) formData = formData.copy(applicant = formData.applicant.copy(name = it)) },
                label = "Nombre (Obligatorio)",
                isError = showErrors && formData.applicant.name.isBlank()
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CustomTextField(
                    value = formData.applicant.firstSurname,
                    onValueChange = { if (it.isOnlyLetters()) formData = formData.copy(applicant = formData.applicant.copy(firstSurname = it)) },
                    label = "1er Apellido",
                    isError = showErrors && formData.applicant.firstSurname.isBlank(),
                    modifier = Modifier.weight(1f)
                )
                CustomTextField(
                    value = formData.applicant.secondSurname,
                    onValueChange = { if (it.isOnlyLetters()) formData = formData.copy(applicant = formData.applicant.copy(secondSurname = it)) },
                    label = "2do Apellido",
                    modifier = Modifier.weight(1f)
                )
            }

            CustomTextField(
                value = formData.applicant.documentId,
                onValueChange = { formData = formData.copy(applicant = formData.applicant.copy(documentId = it)) },
                label = "DNI / NIE / Pasaporte",
                isError = showErrors && formData.applicant.documentId.isBlank()
            )

            // ---  METODO DE RECEPCIÓN ---
            FormSectionTitle("2. ¿Dónde desea recibir su certificado?")

            val isEmailValid = formData.applicant.contact.email.isEmpty() ||
                    android.util.Patterns.EMAIL_ADDRESS.matcher(formData.applicant.contact.email).matches()

            CustomTextField(
                value = formData.applicant.contact.mobilePhone,
                onValueChange = {
                    if (it.all { char -> char.isDigit() }) {
                        val newContact = formData.applicant.contact.copy(mobilePhone = it)
                        formData = formData.copy(applicant = formData.applicant.copy(contact = newContact))
                    }
                },
                label = "Teléfono móvil (para recibir por SMS)",
                isError = false
            )

            CustomTextField(
                value = formData.applicant.contact.email,
                onValueChange = {
                    val newContact = formData.applicant.contact.copy(email = it)
                    formData = formData.copy(applicant = formData.applicant.copy(contact = newContact))
                },
                label = "Correo electrónico (para recibir por EMAIL)",
                isError = showErrors && !isEmailValid
            )

            // --- 3. DOMICILIO DEL SOLICITANTE ---
            FormSectionTitle("3. Domicilio del Solicitante")

            CustomTextField(value = formData.applicant.address.street, onValueChange = { formData = formData.copy(applicant = formData.applicant.copy(address = formData.applicant.address.copy(street = it))) }, label = "Calle / Vía", isError = showErrors && formData.applicant.address.street.isBlank())

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = formData.applicant.address.number, onValueChange = { formData = formData.copy(applicant = formData.applicant.copy(address = formData.applicant.address.copy(number = it))) }, label = { Text("Nº") }, modifier = Modifier.weight(1f))
                OutlinedTextField(value = formData.applicant.address.staircase, onValueChange = { formData = formData.copy(applicant = formData.applicant.copy(address = formData.applicant.address.copy(staircase = it))) }, label = { Text("Esc.") }, modifier = Modifier.weight(1f))
                OutlinedTextField(value = formData.applicant.address.floor, onValueChange = { formData = formData.copy(applicant = formData.applicant.copy(address = formData.applicant.address.copy(floor = it))) }, label = { Text("Planta") }, modifier = Modifier.weight(1f))
                OutlinedTextField(value = formData.applicant.address.door, onValueChange = { formData = formData.copy(applicant = formData.applicant.copy(address = formData.applicant.address.copy(door = it))) }, label = { Text("Puerta") }, modifier = Modifier.weight(1f))
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CustomTextField(value = formData.applicant.address.postalCode, onValueChange = { if (it.all { c -> c.isDigit() }) formData = formData.copy(applicant = formData.applicant.copy(address = formData.applicant.address.copy(postalCode = it))) }, label = "C.P.", isError = showErrors && formData.applicant.address.postalCode.isBlank(), modifier = Modifier.weight(1f))
                CustomTextField(value = formData.applicant.address.city, onValueChange = { formData = formData.copy(applicant = formData.applicant.copy(address = formData.applicant.address.copy(city = it))) }, label = "Localidad", isError = showErrors && formData.applicant.address.city.isBlank(), modifier = Modifier.weight(2f))
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CustomTextField(
                    value = formData.applicant.address.province,
                    onValueChange = {
                        val newAddr = formData.applicant.address.copy(province = it)
                        formData = formData.copy(applicant = formData.applicant.copy(address = newAddr))
                    },
                    label = "Provincia",
                    isError = showErrors && formData.applicant.address.province.isBlank(),
                    modifier = Modifier.weight(1f)
                )
                CustomTextField(
                    value = formData.applicant.address.country,
                    onValueChange = {
                        val newAddr = formData.applicant.address.copy(country = it)
                        formData = formData.copy(applicant = formData.applicant.copy(address = newAddr))
                    },
                    label = "País",
                    isError = showErrors && formData.applicant.address.country.isBlank(),
                    modifier = Modifier.weight(1f)
                )
            }

            // --- 4. DATOS DEL FALLECIDO ---
            FormSectionTitle("4. Datos del Fallecido")

            CustomTextField(
                value = formData.deathRelatedDetails.deceased.name,
                onValueChange = { if (it.isOnlyLetters()) {
                    val dec = formData.deathRelatedDetails.deceased.copy(name = it)
                    formData = formData.copy(deathRelatedDetails = formData.deathRelatedDetails.copy(deceased = dec))
                }},
                label = "Nombre del fallecido",
                isError = showErrors && formData.deathRelatedDetails.deceased.name.isBlank()
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CustomTextField(
                    value = formData.deathRelatedDetails.deceased.firstSurname,
                    onValueChange = { if (it.isOnlyLetters()) {
                        val dec = formData.deathRelatedDetails.deceased.copy(firstSurname = it)
                        formData = formData.copy(deathRelatedDetails = formData.deathRelatedDetails.copy(deceased = dec))
                    }},
                    label = "1er Apellido",
                    isError = showErrors && formData.deathRelatedDetails.deceased.firstSurname.isBlank(),
                    modifier = Modifier.weight(1f)
                )
                CustomTextField(
                    value = formData.deathRelatedDetails.deceased.secondSurname,
                    onValueChange = { if (it.isOnlyLetters()) {
                        val dec = formData.deathRelatedDetails.deceased.copy(secondSurname = it)
                        formData = formData.copy(deathRelatedDetails = formData.deathRelatedDetails.copy(deceased = dec))
                    }},
                    label = "2do Apellido",
                    modifier = Modifier.weight(1f)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CustomTextField(
                    value = formData.deathRelatedDetails.deceased.birthDate,
                    onValueChange = {
                        val dec = formData.deathRelatedDetails.deceased.copy(birthDate = it)
                        formData = formData.copy(deathRelatedDetails = formData.deathRelatedDetails.copy(deceased = dec))
                    },
                    label = "F. Nacim (DD/MM/AAAA)",
                    isError = showErrors && formData.deathRelatedDetails.deceased.birthDate.isNotEmpty()
                            && !formData.deathRelatedDetails.deceased.birthDate.isValidSpanishDate(),
                    modifier = Modifier.weight(1f)
                )
                CustomTextField(
                    value = formData.deathRelatedDetails.deceased.birthCity,
                    onValueChange = {
                        val dec = formData.deathRelatedDetails.deceased.copy(birthCity = it)
                        formData = formData.copy(deathRelatedDetails = formData.deathRelatedDetails.copy(deceased = dec))
                    },
                    label = "Población de Nacimiento",
                    modifier = Modifier.weight(1f)
                )
            }

            CustomTextField(
                value = formData.deathRelatedDetails.deceased.documentId,
                onValueChange = {
                    val dec = formData.deathRelatedDetails.deceased.copy(documentId = it)
                    formData = formData.copy(deathRelatedDetails = formData.deathRelatedDetails.copy(deceased = dec))
                },
                label = "NIF/NIE/Pasaporte (Opcional)"
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CustomTextField(
                    value = formData.deathRelatedDetails.deceased.deathDate,
                    onValueChange = {
                        val dec = formData.deathRelatedDetails.deceased.copy(deathDate = it)
                        formData = formData.copy(deathRelatedDetails = formData.deathRelatedDetails.copy(deceased = dec))
                    },
                    label = "F. Defuncion (DD/MM/AAAA)",
                    isError = showErrors && !formData.deathRelatedDetails.deceased.deathDate.isValidSpanishDate(),
                    modifier = Modifier.weight(1f)
                )
                CustomTextField(
                    value = formData.deathRelatedDetails.deceased.deathCity,
                    onValueChange = {
                        val dec = formData.deathRelatedDetails.deceased.copy(deathCity = it)
                        formData = formData.copy(deathRelatedDetails = formData.deathRelatedDetails.copy(deceased = dec))
                    },
                    label = "Población de Defunción",
                    modifier = Modifier.weight(1f)
                )
            }

            // --- 5. DATOS ADICIONALES ---
            FormSectionTitle("5. Datos adicionales si los conoce")

            CustomTextField(
                value = formData.deathRelatedDetails.lastWillExtra.willDate,
                onValueChange = {
                    val ex = formData.deathRelatedDetails.lastWillExtra.copy(willDate = it)
                    formData = formData.copy(deathRelatedDetails = formData.deathRelatedDetails.copy(lastWillExtra = ex))
                },
                label = "Fecha Testamento (DD/MM/AAAA)",
                isError = showErrors && formData.deathRelatedDetails.lastWillExtra.willDate.isNotEmpty()
                        && !formData.deathRelatedDetails.lastWillExtra.willDate.isValidSpanishDate()
            )

            CustomTextField(
                value = formData.deathRelatedDetails.lastWillExtra.notary,
                onValueChange = {
                    val ex = formData.deathRelatedDetails.lastWillExtra.copy(notary = it)
                    formData = formData.copy(deathRelatedDetails = formData.deathRelatedDetails.copy(lastWillExtra = ex))
                },
                label = "Notario"
            )

            CustomTextField(
                value = formData.deathRelatedDetails.lastWillExtra.grantPlace,
                onValueChange = {
                    val ex = formData.deathRelatedDetails.lastWillExtra.copy(grantPlace = it)
                    formData = formData.copy(deathRelatedDetails = formData.deathRelatedDetails.copy(lastWillExtra = ex))
                },
                label = "Lugar de otorgamiento"
            )

            CustomTextField(
                value = formData.deathRelatedDetails.lastWillExtra.spousesFullName,
                onValueChange = {
                    val ex = formData.deathRelatedDetails.lastWillExtra.copy(spousesFullName = it)
                    formData = formData.copy(deathRelatedDetails = formData.deathRelatedDetails.copy(lastWillExtra = ex))
                },
                label = "Apellidos y nombres cónyuge(s)"
            )

            // --- 6. FIRMA ---
            FormSectionTitle("6. Firma y Autorización")
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Checkbox(
                    checked = formData.signature.postalDeliveryAuthorized,
                    onCheckedChange = { formData = formData.copy(signature = formData.signature.copy(postalDeliveryAuthorized = it)) }
                )
                Text("Autorizo el envío por correo postal", fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- BOTONERA FINAL ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Botón Volver (Naranja)
                Button(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.medium,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF9800), // Naranja
                        contentColor = Color.White
                    )
                ) {
                    Text("VOLVER AL MENÚ")
                }

                // Botón Generar (Azul por defecto)
                Button(
                    onClick = {
                        showErrors = true
                        if (isFormValid) { /* OK */ }
                    },
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text("GENERAR PDF")
                }
            }
        }
    }
}

@Composable
fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isError: Boolean = false,
    modifier: Modifier = Modifier.fillMaxWidth()
) {
    OutlinedTextField(
        value = value,
        onValueChange = { if (!it.contains("\n")) onValueChange(it) },
        label = { Text(label) },
        isError = isError,
        singleLine = true,
        maxLines = 1,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        supportingText = {
            if (isError) {
                val msg = if (label.contains("DD/MM")) "Formato DD/MM/AAAA" else "Campo obligatorio"
                Text(text = msg, color = MaterialTheme.colorScheme.error)
            }
        },
        modifier = modifier
    )
}

@Composable
fun FormSectionTitle(title: String) {
    Text(
        text = title.uppercase(),
        fontSize = 12.sp,
        fontWeight = FontWeight.ExtraBold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 8.dp)
    )
}

fun Modifier.drawScrollbar(state: androidx.compose.foundation.ScrollState): Modifier = drawWithContent {
    drawContent()
    if (state.maxValue > 0) {
        val scrollValue = state.value.toFloat()
        val maxScrollValue = state.maxValue.toFloat()
        val viewportHeight = size.height
        val scrollbarHeight = (viewportHeight / (maxScrollValue + viewportHeight)) * viewportHeight
        val scrollbarTop = (scrollValue / (maxScrollValue + viewportHeight)) * viewportHeight

        drawRect(
            color = Color.Gray.copy(alpha = 0.6f),
            topLeft = Offset(size.width - 6.dp.toPx(), scrollbarTop),
            size = Size(4.dp.toPx(), scrollbarHeight)
        )
    }
}