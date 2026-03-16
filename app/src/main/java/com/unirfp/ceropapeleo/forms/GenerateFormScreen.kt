package com.unirfp.ceropapeleo.forms

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.input.pointer.pointerInput
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
    // Estados locales para trocear la fecha de la firma
    var daySign by remember { mutableStateOf("") }
    var monthSign by remember { mutableStateOf("") }
    var yearSign by remember { mutableStateOf("") } // Solo 2 dígitos porque el 790 ya tiene los 2 primeros
    // Estados para el pago (Añadir al inicio con los demás "remember")
    var selectedPaymentMethod by remember { mutableStateOf("CASH") } // "CASH" o "ACCOUNT"
    var bankEnt by remember { mutableStateOf("") }
    var bankOff by remember { mutableStateOf("") }
    var bankDC by remember { mutableStateOf("") }
    var bankAcc by remember { mutableStateOf("") }

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

            // --- APARTADO: EFECTOS EN EL EXTRANJERO ---
            FormSectionTitle("4. Rellene solo si el certificado ha de tener efectos en el extranjero")

            CustomTextField(
                value = formData.destination.country,
                onValueChange = {
                    formData = formData.copy(
                        destination = formData.destination.copy(country = it)
                    )
                },
                label = "País de destino (Opcional)",
                isError = false
            )

            CustomTextField(
                value = formData.destination.authorityOrEntity,
                onValueChange = {
                    formData = formData.copy(
                        destination = formData.destination.copy(authorityOrEntity = it)
                    )
                },
                label = "Entidad ante la que tiene que surtir efectos (Opcional)",
                isError = false
            )

            // --- 4. DATOS DEL FALLECIDO ---
            FormSectionTitle("5. Datos del Fallecido")

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
            FormSectionTitle("6. Datos adicionales si los conoce")

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
            FormSectionTitle("7. Firma y Autorización")
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Checkbox(
                    checked = formData.signature.postalDeliveryAuthorized,
                    onCheckedChange = { formData = formData.copy(signature = formData.signature.copy(postalDeliveryAuthorized = it)) }
                )
                Text("Autorizo el envío por correo postal", fontSize = 14.sp)
            }

            // LLAMADA A LA FIRMA
            SignaturePad()

            // --- APARTADO: LUGAR Y FECHA DE LA FIRMA ---
            FormSectionTitle("8. Lugar y fecha de la firma")

            CustomTextField(
                value = formData.signature.place,
                onValueChange = {
                    if (it.isOnlyLetters()) {
                        formData = formData.copy(signature = formData.signature.copy(place = it))
                    }
                },
                label = "Ciudad de la firma",
                isError = showErrors && formData.signature.place.isBlank()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text("a", fontSize = 14.sp)
                CustomTextField(
                    value = daySign,
                    onValueChange = { if (it.length <= 2 && it.all { c -> c.isDigit() }) daySign = it },
                    label = "Día",
                    modifier = Modifier.weight(1f),
                    isError = showErrors && daySign.isBlank()
                )
                Text("de", fontSize = 14.sp)
                CustomTextField(
                    value = monthSign,
                    onValueChange = { if (it.isOnlyLetters()) monthSign = it },
                    label = "Mes",
                    modifier = Modifier.weight(2f),
                    isError = showErrors && monthSign.isBlank()
                )
                Text("de 20", fontSize = 14.sp)
                CustomTextField(
                    value = yearSign,
                    onValueChange = { if (it.length <= 2 && it.all { c -> c.isDigit() }) yearSign = it },
                    label = "XX",
                    modifier = Modifier.weight(1f),
                    isError = showErrors && yearSign.length < 2
                )
            }

            // --- SECCIÓN: INGRESO Y FORMA DE PAGO ---
            FormSectionTitle("9. Ingreso y forma de pago")

            OutlinedTextField(
                value = "3,86",
                onValueChange = { },
                label = { Text("IMPORTE euros") },
                readOnly = true,
                suffix = { Text("€") },
                modifier = Modifier.width(180.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.LightGray.copy(alpha = 0.2f),
                    unfocusedContainerColor = Color.LightGray.copy(alpha = 0.2f)
                )
            )

            Spacer(modifier = Modifier.height(8.dp))
            Text("Forma de pago:", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)

            Column {
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    RadioButton(
                        selected = selectedPaymentMethod == "CASH",
                        onClick = { selectedPaymentMethod = "CASH" }
                    )
                    Text("Efectivo")
                }
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    RadioButton(
                        selected = selectedPaymentMethod == "ACCOUNT",
                        onClick = { selectedPaymentMethod = "ACCOUNT" }
                    )
                    Text("E.C. adeudo en cuenta")
                }
            }

            if (selectedPaymentMethod == "ACCOUNT") {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Número de cuenta bancaria:", fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    CustomTextField(
                        value = bankEnt,
                        onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) bankEnt = it },
                        label = "Entid.",
                        modifier = Modifier.weight(1f),
                        isError = showErrors && bankEnt.length < 4
                    )
                    CustomTextField(
                        value = bankOff,
                        onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) bankOff = it },
                        label = "Ofic.",
                        modifier = Modifier.weight(1f),
                        isError = showErrors && bankOff.length < 4
                    )
                    CustomTextField(
                        value = bankDC,
                        onValueChange = { if (it.length <= 2 && it.all { c -> c.isDigit() }) bankDC = it },
                        label = "DC",
                        modifier = Modifier.weight(0.6f),
                        isError = showErrors && bankDC.length < 2
                    )
                    CustomTextField(
                        value = bankAcc,
                        onValueChange = { if (it.length <= 10 && it.all { c -> c.isDigit() }) bankAcc = it },
                        label = "Número de cuenta",
                        modifier = Modifier.weight(2f),
                        isError = showErrors && bankAcc.length < 10
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- BOTONERA FINAL (Movida al final de la Column estructuralmente) ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.medium,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF9800),
                        contentColor = Color.White
                    )
                ) {
                    Text("VOLVER AL MENÚ")
                }

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

// --- COMPOSABLES INDEPENDIENTES (FUERA DE GENERATEFORMSCREEN) ---

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

@Composable
fun SignaturePad(
    modifier: Modifier = Modifier
        .fillMaxWidth()
        .height(200.dp)
) {
    val path = remember { Path() }
    val drawTrigger = remember { mutableStateOf(0) }

    Column {
        Text(
            text = "Dibuje su firma aquí:",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        Box(
            modifier = modifier
                .background(Color.White, shape = MaterialTheme.shapes.small)
                .border(1.dp, Color.Gray, shape = MaterialTheme.shapes.small)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset -> path.moveTo(offset.x, offset.y) },
                        onDrag = { change, _ ->
                            path.lineTo(change.position.x, change.position.y)
                            drawTrigger.value++
                        }
                    )
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawTrigger.value
                drawPath(
                    path = path,
                    color = Color.Black,
                    style = Stroke(width = 4f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
            }
        }

        TextButton(
            onClick = { path.reset(); drawTrigger.value++ },
            modifier = Modifier.align(androidx.compose.ui.Alignment.End)
        ) {
            Text("Limpiar firma", color = Color.Red)
        }
    }
}