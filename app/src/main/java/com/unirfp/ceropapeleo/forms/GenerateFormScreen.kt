package com.unirfp.ceropapeleo.forms

import android.content.Intent
import android.net.Uri
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.unirfp.ceropapeleo.api.PdfRepository
import com.unirfp.ceropapeleo.model.GenerateRequest
import kotlinx.coroutines.launch
import android.os.Environment
import android.widget.Toast
import com.unirfp.ceropapeleo.api.UserDataMapper
import com.unirfp.ceropapeleo.utils.DownloadUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import android.util.Log
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.ResolverStyle
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.ExperimentalFoundationApi
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Calendar
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas as AndroidCanvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.graphics.Path as AndroidPath
import android.util.Base64
import android.view.MotionEvent
import android.view.View
import androidx.compose.ui.viewinterop.AndroidView
import java.io.ByteArrayOutputStream

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun GenerateFormScreen(navController: NavController) {
    var formData by remember { mutableStateOf(GenerateRequest()) }
    val scrollState = rememberScrollState()
    var showErrors by remember { mutableStateOf(false) }
    val deathDateRequester = remember { BringIntoViewRequester() }
    val emailRequester = remember { BringIntoViewRequester() }
    val signatureRequester = remember { BringIntoViewRequester() }
    val nameRequester = remember { BringIntoViewRequester() }
    val firstSurnameRequester = remember { BringIntoViewRequester() }
    val documentIdRequester = remember { BringIntoViewRequester() }
    val streetRequester = remember { BringIntoViewRequester() }
    val postalCodeRequester = remember { BringIntoViewRequester() }
    val cityRequester = remember { BringIntoViewRequester() }
    val provinceRequester = remember { BringIntoViewRequester() }
    val countryRequester = remember { BringIntoViewRequester() }
    val deceasedNameRequester = remember { BringIntoViewRequester() }
    val deceasedFirstSurnameRequester = remember { BringIntoViewRequester() }
    val signaturePlaceRequester = remember { BringIntoViewRequester() }

    // Estados para el pago
    var selectedPaymentMethod by remember { mutableStateOf("CASH") } // "CASH" o "ACCOUNT"
    var bankEnt by remember { mutableStateOf("") }
    var bankOff by remember { mutableStateOf("") }
    var bankDC by remember { mutableStateOf("") }
    var bankAcc by remember { mutableStateOf("") }

    // Para poder lanzar la petición:
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val repository = remember { PdfRepository() }
    var signaturePadView by remember { mutableStateOf<SignaturePadView?>(null) }

    // --- HELPERS DE VALIDACIÓN / SANEADO ---

    fun String.sanitizeLetters(max: Int): String =
        this.filter { it.isLetter() || it.isWhitespace() || it == '-' || it == '\'' }
            .take(max)

    fun String.sanitizeDigits(max: Int): String =
        this.filter { it.isDigit() }.take(max)

    fun String.sanitizeDate(max: Int = 10): String =
        this.filter { it.isDigit() || it == '/' }.take(max)

    fun String.sanitizeAlphanumeric(max: Int): String =
        this.filter { it.isLetterOrDigit() }.take(max)

    val spanishDateFormatter: DateTimeFormatter =
        DateTimeFormatter.ofPattern("dd/MM/uuuu")
            .withResolverStyle(ResolverStyle.STRICT)

    fun String.toLocalDateOrNull(): LocalDate? {
        return try {
            LocalDate.parse(this, spanishDateFormatter)
        } catch (e: Exception) {
            null
        }
    }
    fun String.isValidSpanishDate(): Boolean = this.toLocalDateOrNull() != null

    // Para permitir solo un año por adelantado para la fecha de la firma
    val todayMillis = remember {
        val calendar = java.util.Calendar.getInstance()
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        calendar.timeInMillis
    }

    val oneYearFromTodayMillis = remember {
        val calendar = java.util.Calendar.getInstance()
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        calendar.add(java.util.Calendar.YEAR, 1)
        calendar.timeInMillis
    }

    val email = formData.applicant.contact.email

    val isEmailValid = email.isBlank() ||
            android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()

    // Estados "touched" para validar campos al "tocarlos" y no introducir datos válidos
    var birthDateTouched by remember { mutableStateOf(false) }
    var deathDateTouched by remember { mutableStateOf(false) }
    var willDateTouched by remember { mutableStateOf(false) }
    var emailTouched by remember { mutableStateOf(false) }

    // Validación cronológica de fechas de nacimiento, defunción y testamento
    val birthDateValue = formData.deathRelatedDetails.deceased.birthDate
    val deathDateValue = formData.deathRelatedDetails.deceased.deathDate
    val willDateValue = formData.deathRelatedDetails.lastWillExtra.willDate

    val birthDateParsed = birthDateValue.toLocalDateOrNull()
    val deathDateParsed = deathDateValue.toLocalDateOrNull()
    val willDateParsed = willDateValue.toLocalDateOrNull()

    val isBirthBeforeDeath =
        birthDateParsed == null || deathDateParsed == null || birthDateParsed.isBefore(deathDateParsed)

    val isWillBeforeDeath =
        willDateParsed == null || deathDateParsed == null || willDateParsed.isBefore(deathDateParsed)

    val isBirthBeforeWill =
        birthDateParsed == null || willDateParsed == null || birthDateParsed.isBefore(willDateParsed)

    val birthDateError = when {
        birthDateValue.isBlank() -> null
        !birthDateValue.isValidSpanishDate() -> "Fecha de nacimiento inválida"
        !isBirthBeforeDeath -> "La fecha de nacimiento debe ser anterior a la de defunción"
        !isBirthBeforeWill -> "La fecha de nacimiento debe ser anterior a la del testamento"
        else -> null
    }

    val deathDateError = when {
        deathDateValue.isBlank() -> "Campo obligatorio"
        !deathDateValue.isValidSpanishDate() -> "Fecha de defunción inválida"
        !isBirthBeforeDeath -> "La fecha de defunción debe ser posterior a la de nacimiento"
        !isWillBeforeDeath -> "La fecha de defunción debe ser posterior a la del testamento"
        else -> null
    }

    val willDateError = when {
        willDateValue.isBlank() -> null
        !willDateValue.isValidSpanishDate() -> "Fecha del testamento inválida"
        !isWillBeforeDeath -> "La fecha del testamento debe ser anterior a la de defunción"
        !isBirthBeforeWill -> "La fecha del testamento debe ser posterior a la de nacimiento"
        else -> null
    }

    val isSignatureDateValid = formData.signature.date.isValidSpanishDate()

    // Para el CP
    val postalCode = formData.applicant.address.postalCode
    val countryNormalized = formData.applicant.address.country.trim().lowercase()

    val isPostalCodeValid = when (countryNormalized) {
        "españa", "spain" -> postalCode.length == 5
        else -> postalCode.isNotBlank()
    }

    // --- LÓGICA DE VALIDACIÓN GLOBAL ---
    val isFormValid = formData.applicant.name.isNotBlank() &&
            formData.applicant.firstSurname.isNotBlank() &&
            formData.applicant.documentId.isNotBlank() &&
            formData.applicant.address.street.isNotBlank() &&
            formData.applicant.address.city.isNotBlank() &&
            formData.applicant.address.province.isNotBlank() &&
            formData.applicant.address.country.isNotBlank() &&
            formData.deathRelatedDetails.deceased.name.isNotBlank() &&
            formData.deathRelatedDetails.deceased.firstSurname.isNotBlank() &&
            formData.deathRelatedDetails.deceased.deathDate.isValidSpanishDate() &&
            (formData.deathRelatedDetails.deceased.birthDate.isBlank() ||
                    formData.deathRelatedDetails.deceased.birthDate.isValidSpanishDate()) &&
            (formData.deathRelatedDetails.lastWillExtra.willDate.isBlank() ||
                    formData.deathRelatedDetails.lastWillExtra.willDate.isValidSpanishDate()) &&
            birthDateError == null &&
            deathDateError == null &&
            willDateError == null &&
            formData.signature.place.isNotBlank() &&
            isSignatureDateValid &&
            (signaturePadView?.hasSignature() == true) &&
            isPostalCodeValid &&
            isEmailValid

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
            Text("Complete los campos para generar el PDF de Actos de Última Voluntad listo para entregar", fontSize = 14.sp)

            // --- 1. DATOS DEL SOLICITANTE ---
            FormSectionTitle("1. Datos del Solicitante")

            CustomTextField(
                value = formData.applicant.name,
                onValueChange = {
                    formData = formData.copy(
                        applicant = formData.applicant.copy(name = it.sanitizeLetters(30))
                    )
                },
                label = "Nombre*",
                isError = showErrors && formData.applicant.name.isBlank(),
                maxLength = 30,
                modifier = Modifier
                    .fillMaxWidth()
                    .bringIntoViewRequester(nameRequester)
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CustomTextField(
                    value = formData.applicant.firstSurname,
                    onValueChange = {
                        formData = formData.copy(
                            applicant = formData.applicant.copy(firstSurname = it.sanitizeLetters(40))
                        )
                    },
                    label = "1er apellido*",
                    isError = showErrors && formData.applicant.firstSurname.isBlank(),
                    modifier = Modifier
                        .weight(1f)
                        .bringIntoViewRequester(firstSurnameRequester),
                    maxLength = 40
                )
                CustomTextField(
                    value = formData.applicant.secondSurname,
                    onValueChange = {
                        formData = formData.copy(
                            applicant = formData.applicant.copy(secondSurname = it.sanitizeLetters(40))
                        )
                    },
                    label = "2do apellido",
                    modifier = Modifier.weight(1f),
                    maxLength = 40
                )
            }

            CustomTextField(
                value = formData.applicant.documentId,
                onValueChange = {
                    formData = formData.copy(
                        applicant = formData.applicant.copy(documentId = it.trim().take(15))
                    )
                },
                label = "DNI / NIE / Pasaporte*",
                isError = showErrors && formData.applicant.documentId.isBlank(),
                maxLength = 15,
                modifier = Modifier
                    .fillMaxWidth()
                    .bringIntoViewRequester(documentIdRequester)
            )

            // ---  MÉTODO DE RECEPCIÓN ---
            FormSectionTitle("2. ¿Dónde desea recibir su certificado?")

            CustomTextField(
                value = formData.applicant.contact.mobilePhone,
                onValueChange = {
                    val newContact = formData.applicant.contact.copy(
                        mobilePhone = it.sanitizeDigits(15)
                    )
                    formData = formData.copy(applicant = formData.applicant.copy(contact = newContact))
                },
                label = "Teléfono móvil (para recibir por SMS)",
                isError = false,
                maxLength = 15
            )
            CustomTextField(
                value = email,
                onValueChange = {
                    val newContact = formData.applicant.contact.copy(email = it.take(60))
                    formData = formData.copy(applicant = formData.applicant.copy(contact = newContact))
                },
                label = "Correo electrónico (para recibir por EMAIL)",
                isError = (emailTouched || showErrors) && !isEmailValid,
                errorMessage = "Introduce un email válido",
                onBlur = { emailTouched = true },
                maxLength = 60,
                modifier = Modifier
                    .fillMaxWidth()
                    .bringIntoViewRequester(emailRequester)
            )

            // --- 3. DOMICILIO DEL SOLICITANTE ---
            FormSectionTitle("3. Domicilio del Solicitante")

            CustomTextField(
                value = formData.applicant.address.street,
                onValueChange = {
                    formData = formData.copy(
                        applicant = formData.applicant.copy(
                            address = formData.applicant.address.copy(street = it.take(60))
                        )
                    )
                },
                label = "Calle/Plaza/Avenida*",
                isError = showErrors && formData.applicant.address.street.isBlank(),
                maxLength = 60,
                modifier = Modifier
                    .fillMaxWidth()
                    .bringIntoViewRequester(streetRequester)
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = formData.applicant.address.number,
                    onValueChange = {
                        formData = formData.copy(
                            applicant = formData.applicant.copy(
                                address = formData.applicant.address.copy(number = it.sanitizeDigits(5))
                            )
                        )
                    },
                    label = { Text("Nº") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = formData.applicant.address.staircase,
                    onValueChange = {
                        formData = formData.copy(
                            applicant = formData.applicant.copy(
                                address = formData.applicant.address.copy(
                                    staircase = it.sanitizeAlphanumeric(3)
                                )
                            )
                        )
                    },
                    label = { Text("Esc.") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = formData.applicant.address.floor,
                    onValueChange = {
                        formData = formData.copy(
                            applicant = formData.applicant.copy(
                                address = formData.applicant.address.copy(floor = it.sanitizeDigits(3))
                            )
                        )
                    },
                    label = { Text("Piso") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = formData.applicant.address.door,
                    onValueChange = {
                        formData = formData.copy(
                            applicant = formData.applicant.copy(
                                address = formData.applicant.address.copy(
                                    door = it.sanitizeAlphanumeric(3)
                                )
                            )
                        )
                    },
                    label = { Text("Puerta") },
                    modifier = Modifier.weight(1f)
                )            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CustomTextField(
                    value = formData.applicant.address.postalCode,
                    onValueChange = {
                        val sanitizedPostalCode = when (countryNormalized) {
                            "españa", "spain" -> it.filter { c -> c.isDigit() }.take(5)
                            else -> it.filter { c -> c.isLetterOrDigit() || c == ' ' || c == '-' }.take(10)
                        }

                        formData = formData.copy(
                            applicant = formData.applicant.copy(
                                address = formData.applicant.address.copy(postalCode = sanitizedPostalCode)
                            )
                        )
                    },
                    label = "C.P.*",
                    isError = showErrors && !isPostalCodeValid,
                    errorMessage = when {
                        postalCode.isBlank() -> "Campo obligatorio"
                        countryNormalized in listOf("españa", "spain") && postalCode.length != 5 ->
                            "Debe tener 5 dígitos"
                        else -> "Código postal inválido"
                    },
                    modifier = Modifier
                        .weight(1f)
                        .bringIntoViewRequester(postalCodeRequester),
                    maxLength = if (countryNormalized in listOf("españa", "spain")) 5 else 10
                )
                CustomTextField(
                    value = formData.applicant.address.city,
                    onValueChange = {
                        formData = formData.copy(
                            applicant = formData.applicant.copy(
                                address = formData.applicant.address.copy(city = it.sanitizeLetters(40))
                            )
                        )
                    },
                    label = "Municipio*",
                    isError = showErrors && formData.applicant.address.city.isBlank(),
                    modifier = Modifier
                        .weight(2f)
                        .bringIntoViewRequester(cityRequester),
                    maxLength = 40
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CustomTextField(
                    value = formData.applicant.address.province,
                    onValueChange = {
                        formData = formData.copy(
                            applicant = formData.applicant.copy(
                                address = formData.applicant.address.copy(province = it.sanitizeLetters(40))
                            )
                        )
                    },
                    label = "Provincia*",
                    isError = showErrors && formData.applicant.address.province.isBlank(),
                    modifier = Modifier
                        .weight(2f)
                        .bringIntoViewRequester(provinceRequester),
                    maxLength = 40
                )
                CustomTextField(
                    value = formData.applicant.address.country,
                    onValueChange = {
                        formData = formData.copy(
                            applicant = formData.applicant.copy(
                                address = formData.applicant.address.copy(country = it.sanitizeLetters(40))
                            )
                        )
                    },
                    label = "País*",
                    isError = showErrors && formData.applicant.address.country.isBlank(),
                    modifier = Modifier
                        .weight(2f)
                        .bringIntoViewRequester(countryRequester),
                    maxLength = 40
                )
            }

            // --- APARTADO: EFECTOS EN EL EXTRANJERO ---
            FormSectionTitle("4. Rellene solo si el certificado ha de tener efectos en el extranjero")

            CustomTextField(
                value = formData.destination.country,
                onValueChange = {
                    formData = formData.copy(
                        destination = formData.destination.copy(country = it.sanitizeLetters(40))
                    )
                },
                label = "País de destino (opcional)",
                isError = false,
                maxLength = 40
            )

            CustomTextField(
                value = formData.destination.authorityOrEntity,
                onValueChange = {
                    formData = formData.copy(
                        destination = formData.destination.copy(authorityOrEntity = it.take(60))
                    )
                },
                label = "Entidad ante la que tiene que surtir efectos (opcional)",
                isError = false,
                maxLength = 60
            )

            // --- 4. DATOS DEL FALLECIDO ---
            FormSectionTitle("5. Datos del Fallecido")

            CustomTextField(
                value = formData.deathRelatedDetails.deceased.name,
                onValueChange = {
                    val dec = formData.deathRelatedDetails.deceased.copy(
                        name = it.sanitizeLetters(30)
                    )
                    formData = formData.copy(
                        deathRelatedDetails = formData.deathRelatedDetails.copy(deceased = dec)
                    )
                },
                label = "Nombre del fallecido*",
                isError = showErrors && formData.deathRelatedDetails.deceased.name.isBlank(),
                maxLength = 30,
                modifier = Modifier
                    .fillMaxWidth()
                    .bringIntoViewRequester(deceasedNameRequester)
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CustomTextField(
                    value = formData.deathRelatedDetails.deceased.firstSurname,
                    onValueChange = {
                        val dec = formData.deathRelatedDetails.deceased.copy(
                            firstSurname = it.sanitizeLetters(30)
                        )
                        formData = formData.copy(
                            deathRelatedDetails = formData.deathRelatedDetails.copy(deceased = dec)
                        )
                    },
                    label = "1er apellido*",
                    isError = showErrors && formData.deathRelatedDetails.deceased.firstSurname.isBlank(),
                    maxLength = 30,
                    modifier = Modifier
                        .weight(1f)
                        .bringIntoViewRequester(deceasedFirstSurnameRequester)
                )
                CustomTextField(
                    value = formData.deathRelatedDetails.deceased.secondSurname,
                    onValueChange = {
                        val dec = formData.deathRelatedDetails.deceased.copy(
                            secondSurname = it.sanitizeLetters(30)
                        )
                        formData = formData.copy(
                            deathRelatedDetails = formData.deathRelatedDetails.copy(deceased = dec)
                        )
                    },
                    label = "2do apellido",
                    isError = false,
                    maxLength = 30,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DatePickerField(
                    value = formData.deathRelatedDetails.deceased.birthDate,
                    label = "Fecha de nacimiento",
                    isError = (birthDateTouched || showErrors) && birthDateError != null,
                    errorMessage = birthDateError ?: "",
                    onDateSelected = {
                        val dec = formData.deathRelatedDetails.deceased.copy(birthDate = it)
                        formData = formData.copy(
                            deathRelatedDetails = formData.deathRelatedDetails.copy(deceased = dec)
                        )
                        birthDateTouched = true
                    },
                    minDateMillis = null,
                    maxDateMillis = todayMillis,
                    modifier = Modifier.weight(1f)
                )

                CustomTextField(
                    value = formData.deathRelatedDetails.deceased.birthCity,
                    onValueChange = {
                        val dec = formData.deathRelatedDetails.deceased.copy(
                            birthCity = it.sanitizeLetters(40)
                        )
                        formData = formData.copy(
                            deathRelatedDetails = formData.deathRelatedDetails.copy(deceased = dec)
                        )
                    },
                    label = "Población de nacimiento",
                    modifier = Modifier.weight(1f),
                    maxLength = 40
                )
            }

            CustomTextField(
                value = formData.deathRelatedDetails.deceased.documentId,
                onValueChange = {
                    val dec = formData.deathRelatedDetails.deceased.copy(
                        documentId = it.trim().take(15)
                    )
                    formData = formData.copy(
                        deathRelatedDetails = formData.deathRelatedDetails.copy(deceased = dec)
                    )
                },
                label = "NIF/NIE/Pasaporte",
                isError = false,
                maxLength = 15
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DatePickerField(
                    value = formData.deathRelatedDetails.deceased.deathDate,
                    label = "Fecha de defunción*",
                    isError = (deathDateTouched || showErrors) && deathDateError != null,
                    errorMessage = deathDateError ?: "",
                    onDateSelected = {
                        val dec = formData.deathRelatedDetails.deceased.copy(deathDate = it)
                        formData = formData.copy(
                            deathRelatedDetails = formData.deathRelatedDetails.copy(deceased = dec)
                        )
                        deathDateTouched = true
                    },
                    minDateMillis = null,
                    maxDateMillis = todayMillis,
                    modifier = Modifier
                        .weight(1f)
                        .bringIntoViewRequester(deathDateRequester)
                )
                CustomTextField(
                    value = formData.deathRelatedDetails.deceased.deathCity,
                    onValueChange = {
                        val dec = formData.deathRelatedDetails.deceased.copy(
                            deathCity = it.sanitizeLetters(40)
                        )
                        formData = formData.copy(
                            deathRelatedDetails = formData.deathRelatedDetails.copy(deceased = dec)
                        )
                    },
                    label = "Población de defunción",
                    modifier = Modifier.weight(1f),
                    maxLength = 40
                )
            }

            // --- 5. DATOS ADICIONALES ---
            FormSectionTitle("6. Datos adicionales si los conoce")
            DatePickerField(
                value = formData.deathRelatedDetails.lastWillExtra.willDate,
                label = "Fecha del testamento",
                isError = (willDateTouched || showErrors) && willDateError != null,
                errorMessage = willDateError ?: "",
                onDateSelected = {
                    val dec = formData.deathRelatedDetails.lastWillExtra.copy(willDate = it)
                    formData = formData.copy(
                        deathRelatedDetails = formData.deathRelatedDetails.copy(lastWillExtra = dec)
                    )
                    willDateTouched = true
                },
                minDateMillis = null,
                maxDateMillis = todayMillis
            )
            CustomTextField(
                value = formData.deathRelatedDetails.lastWillExtra.notary,
                onValueChange = {
                    val dec = formData.deathRelatedDetails.lastWillExtra.copy(
                        notary = it.sanitizeLetters(40)
                    )
                    formData = formData.copy(
                        deathRelatedDetails = formData.deathRelatedDetails.copy(lastWillExtra = dec)
                    )
                },
                label = "Notario",
                maxLength = 40
            )
            CustomTextField(
                value = formData.deathRelatedDetails.lastWillExtra.grantPlace,
                onValueChange = {
                    val dec = formData.deathRelatedDetails.lastWillExtra.copy(
                        grantPlace = it.sanitizeLetters(40)
                    )
                    formData = formData.copy(
                        deathRelatedDetails = formData.deathRelatedDetails.copy(lastWillExtra = dec)
                    )
                },
                label = "Lugar de otorgamiento",
                maxLength = 40
            )
            CustomTextField(
                value = formData.deathRelatedDetails.lastWillExtra.spousesFullName,
                onValueChange = {
                    val dec = formData.deathRelatedDetails.lastWillExtra.copy(
                        spousesFullName = it.sanitizeLetters(40)
                    )
                    formData = formData.copy(
                        deathRelatedDetails = formData.deathRelatedDetails.copy(lastWillExtra = dec)
                    )
                },
                label = "Apellidos y nombre cónyuge",
                maxLength = 40
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
            SignaturePad(
                onSignatureChanged = {
                    // La firma se ha modificado, pero no exportamos aún a Base64
                },
                onSignatureCleared = {
                    formData = formData.copy(
                        signature = formData.signature.copy(imageBase64 = "")
                    )
                },
                onPadReady = { view ->
                    signaturePadView = view
                }
            )

            // --- APARTADO: LUGAR Y FECHA DE LA FIRMA ---
            FormSectionTitle("8. Lugar y fecha de la firma")
            CustomTextField(
                value = formData.signature.place,
                onValueChange = {
                    val newSignature = formData.signature.copy(
                        place = it.sanitizeLetters(40)
                    )
                    formData = formData.copy(signature = newSignature)
                },
                label = "Ciudad de la firma*",
                isError = showErrors && formData.signature.place.isBlank(),
                maxLength = 40,
                modifier = Modifier
                    .fillMaxWidth()
                    .bringIntoViewRequester(signaturePlaceRequester)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .bringIntoViewRequester(signatureRequester),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text("a", fontSize = 14.sp)

                DatePickerField(
                    value = formData.signature.date,
                    label = "Fecha de firma*",
                    isError = showErrors && !formData.signature.date.isValidSpanishDate(),
                    errorMessage = "Seleccione una fecha válida",
                    onDateSelected = {
                        formData = formData.copy(
                            signature = formData.signature.copy(date = it)
                        )
                    },
                    minDateMillis = null,
                    maxDateMillis = oneYearFromTodayMillis,
                    modifier = Modifier.weight(1f)
                )
            }

            // --- SECCIÓN INGRESO Y FORMA DE PAGO ---
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
                        onClick = {
                            selectedPaymentMethod = "CASH"
                            formData = formData.copy(
                                payment = formData.payment.copy(paymentMethod = "CASH")
                            )
                        }
                    )
                    Text("Efectivo")
                }
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    RadioButton(
                        selected = selectedPaymentMethod == "ACCOUNT",
                        onClick = {
                            selectedPaymentMethod = "ACCOUNT"
                            formData = formData.copy(
                                payment = formData.payment.copy(paymentMethod = "ACCOUNT")
                            )
                        }
                    )
                    Text("E.C. adeudo en cuenta")
                }
            }

            if (selectedPaymentMethod == "ACCOUNT") {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Número de cuenta bancaria:", fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = androidx.compose.ui.Alignment.Top
                ) {
                    CustomTextField(
                        value = bankEnt,
                        onValueChange = {
                            if (it.length <= 4 && it.all { c -> c.isDigit() }) {
                                bankEnt = it
                                formData = formData.copy(
                                    payment = formData.payment.copy(bankEnt = it)
                                )
                            }
                        },
                        label = "Ent.*",
                        modifier = Modifier.weight(1f),
                        isError = showErrors && bankEnt.length < 4,
                        errorMessage = "4 dígitos",
                        maxLength = 4
                    )

                    CustomTextField(
                        value = bankOff,
                        onValueChange = {
                            if (it.length <= 4 && it.all { c -> c.isDigit() }) {
                                bankOff = it
                                formData = formData.copy(
                                    payment = formData.payment.copy(bankOff = it)
                                )
                            }
                        },
                        label = "Ofic.*",
                        modifier = Modifier.weight(1f),
                        isError = showErrors && bankOff.length < 4,
                        errorMessage = "4 dígitos",
                        maxLength = 4
                    )

                    CustomTextField(
                        value = bankDC,
                        onValueChange = {
                            if (it.length <= 2 && it.all { c -> c.isDigit() }) {
                                bankDC = it
                                formData = formData.copy(
                                    payment = formData.payment.copy(bankDC = it)
                                )
                            }
                        },
                        label = "DC*",
                        modifier = Modifier.weight(0.8f),
                        isError = showErrors && bankDC.length < 2,
                        errorMessage = "2 dígitos",
                        maxLength = 2
                    )

                    CustomTextField(
                        value = bankAcc,
                        onValueChange = {
                            if (it.length <= 10 && it.all { c -> c.isDigit() }) {
                                bankAcc = it
                                formData = formData.copy(
                                    payment = formData.payment.copy(bankAcc = it)
                                )
                            }
                        },
                        label = "Cuenta*",
                        modifier = Modifier.weight(1.8f),
                        isError = showErrors && bankAcc.length < 10,
                        errorMessage = "10 dígitos",
                        maxLength = 10
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

                        if (!isFormValid) {
                            scope.launch {
                                when {
                                    formData.applicant.name.isBlank() -> nameRequester.bringIntoView()
                                    formData.applicant.firstSurname.isBlank() -> firstSurnameRequester.bringIntoView()
                                    formData.applicant.documentId.isBlank() -> documentIdRequester.bringIntoView()
                                    formData.applicant.address.street.isBlank() -> streetRequester.bringIntoView()
                                    !isPostalCodeValid -> postalCodeRequester.bringIntoView()
                                    formData.applicant.address.city.isBlank() -> cityRequester.bringIntoView()
                                    formData.applicant.address.province.isBlank() -> provinceRequester.bringIntoView()
                                    formData.applicant.address.country.isBlank() -> countryRequester.bringIntoView()
                                    !isEmailValid -> emailRequester.bringIntoView()
                                    formData.deathRelatedDetails.deceased.name.isBlank() -> deceasedNameRequester.bringIntoView()
                                    formData.deathRelatedDetails.deceased.firstSurname.isBlank() -> deceasedFirstSurnameRequester.bringIntoView()
                                    deathDateValue.isBlank() || deathDateError != null -> deathDateRequester.bringIntoView()
                                    formData.signature.place.isBlank() -> signaturePlaceRequester.bringIntoView()
                                    !isSignatureDateValid -> signatureRequester.bringIntoView()
                                }
                            }

                            Toast.makeText(
                                context,
                                "Hay errores en el formulario. Revise los campos marcados en rojo",
                                Toast.LENGTH_LONG
                            ).show()
                        } else {
                            scope.launch {
                                try {
                                    val pdfFile = findLatestDownloadedPdf()

                                    if (pdfFile == null || !pdfFile.exists()) {
                                        Toast.makeText(
                                            context,
                                            "No se encontró el PDF oficial en Descargas",
                                            Toast.LENGTH_LONG
                                        ).show()
                                        return@launch
                                    }

                                    Toast.makeText(
                                        context,
                                        "PDF base encontrado",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                    val signatureBase64 = signaturePadView?.exportSignatureBase64().orEmpty()

                                    formData = formData.copy(
                                        signature = formData.signature.copy(imageBase64 = signatureBase64)
                                    )

                                    val updatedFormData = formData.copy(
                                        signature = formData.signature.copy(imageBase64 = signatureBase64)
                                    )

                                    val dataMap = UserDataMapper.toFlatMap(updatedFormData)

                                    dataMap.forEach { (key, value) ->
                                        Log.d("PDF_MAPPING", "$key = $value")
                                    }

                                    Toast.makeText(
                                        context,
                                        "Enviando PDF al backend...",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                    val responseBody = withContext(Dispatchers.IO) {
                                        repository.uploadAndFillPdf(
                                            pdfFile,
                                            dataMap,
                                            signatureBase64
                                        )
                                    }

                                    Toast.makeText(
                                        context,
                                        if (responseBody != null) "Respuesta PDF recibida" else "Respuesta vacía del backend",
                                        Toast.LENGTH_LONG
                                    ).show()

                                    if (responseBody != null) {
                                        val savedUri = DownloadUtils.saveApiPdfToDisk(
                                            context,
                                            responseBody,
                                            "Solicitud_Final_${System.currentTimeMillis()}.pdf"
                                        )

                                        if (savedUri != null) {
                                            Toast.makeText(
                                                context,
                                                "PDF guardado en Descargas",
                                                Toast.LENGTH_LONG
                                            ).show()

                                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                                setDataAndType(Uri.parse(savedUri), "application/pdf")
                                                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                                            }
                                            // En caso de que el dispositivo móvil no tuviera una app para abrir el PDF
                                            val chooser = Intent.createChooser(intent, "Abrir PDF")

                                            try {
                                                context.startActivity(chooser)
                                            } catch (e: Exception) {
                                                Toast.makeText(
                                                    context,
                                                    "No hay aplicación para abrir PDF",
                                                    Toast.LENGTH_LONG
                                                ).show()
                                            }

                                        } else {
                                            Toast.makeText(
                                                context,
                                                "Error al guardar el PDF",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                    } else {
                                        Toast.makeText(
                                            context,
                                            "El backend no respondió correctamente",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }

                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    Toast.makeText(
                                        context,
                                        "Error al generar el PDF",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                        }
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

private fun findLatestDownloadedPdf(): File? {
    val downloadsDir = Environment.getExternalStoragePublicDirectory(
        Environment.DIRECTORY_DOWNLOADS
    )

    return downloadsDir.listFiles()
        ?.filter { file ->
            file.isFile &&
                    file.name.startsWith("formulario-790-006_es_es") &&
                    file.extension.equals("pdf", ignoreCase = true)
        }
        ?.maxByOrNull { it.lastModified() }
}

// --- COMPOSABLES INDEPENDIENTES (FUERA DE GENERATEFORMSCREEN) ---

@Composable
fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isError: Boolean = false,
    errorMessage: String = "Campo obligatorio",
    maxLength: Int = Int.MAX_VALUE,
    onBlur: (() -> Unit)? = null,
    modifier: Modifier = Modifier.fillMaxWidth()
) {
    var hadFocus by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = value,
        onValueChange = {
            if (!it.contains("\n") && it.length <= maxLength) {
                onValueChange(it)
            }
        },
        label = { Text(label) },
        isError = isError,
        singleLine = true,
        maxLines = 1,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        supportingText = {
            if (isError) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        modifier = modifier.onFocusChanged { focusState ->
            if (focusState.isFocused) {
                hadFocus = true
            } else if (hadFocus) {
                onBlur?.invoke()
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerField(
    value: String,
    label: String,
    isError: Boolean = false,
    errorMessage: String = "Fecha inválida",
    onDateSelected: (String) -> Unit,
    minDateMillis: Long? = null,
    maxDateMillis: Long? = null,
    modifier: Modifier = Modifier.fillMaxWidth()
) {
    var showDatePicker by remember { mutableStateOf(false) }

    if (showDatePicker) {
        val minYear = minDateMillis?.let {
            Calendar.getInstance().apply { timeInMillis = it }.get(Calendar.YEAR)
        } ?: 1900

        val maxYear = maxDateMillis?.let {
            Calendar.getInstance().apply { timeInMillis = it }.get(Calendar.YEAR)
        } ?: 2100

        val datePickerState = rememberDatePickerState(
            yearRange = minYear..maxYear,
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    val afterMin = minDateMillis?.let { utcTimeMillis >= it } ?: true
                    val beforeMax = maxDateMillis?.let { utcTimeMillis <= it } ?: true
                    return afterMin && beforeMax
                }
            }
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val selectedMillis = datePickerState.selectedDateMillis
                        if (selectedMillis != null) {
                            val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                            onDateSelected(formatter.format(Date(selectedMillis)))
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Box(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            isError = isError,
            supportingText = {
                if (isError) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            trailingIcon = {
                Text("📅")
            },
            modifier = Modifier.fillMaxWidth()
        )

        Box(
            modifier = Modifier
                .matchParentSize()
                .padding(bottom = if (isError) 24.dp else 0.dp)
        ) {
            Surface(
                modifier = Modifier.matchParentSize(),
                color = Color.Transparent,
                onClick = { showDatePicker = true }
            ) {}
        }
    }
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
        .height(200.dp),
    onSignatureChanged: () -> Unit,
    onSignatureCleared: () -> Unit = {},
    onPadReady: (SignaturePadView) -> Unit = {}
) {
    var signatureView by remember { mutableStateOf<SignaturePadView?>(null) }

    Column {
        Text(
            text = "Dibuje su firma aquí:",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        AndroidView(
            modifier = modifier,
            factory = { context ->
                SignaturePadView(context).also { view ->
                    signatureView = view
                    onPadReady(view)

                    view.onSigned = {
                        onSignatureChanged()
                    }

                    view.onCleared = {
                        onSignatureCleared()
                    }
                }
            }
        )

        TextButton(
            onClick = {
                signatureView?.clear()
                onSignatureCleared()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Limpiar firma")
        }
    }
}

class SignaturePadView(context: Context) : View(context) {

    private val paint = Paint().apply {
        color = AndroidColor.BLACK
        style = Paint.Style.STROKE
        strokeWidth = 6f
        isAntiAlias = true
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    private val borderPaint = Paint().apply {
        color = AndroidColor.LTGRAY
        style = Paint.Style.STROKE
        strokeWidth = 2f
        isAntiAlias = true
    }

    private val path = AndroidPath()
    private var hasSignature = false

    private var lastX = 0f
    private var lastY = 0f

    var onSigned: (() -> Unit)? = null
    var onCleared: (() -> Unit)? = null

    init {
        setBackgroundColor(AndroidColor.WHITE)
    }

    override fun onDraw(canvas: AndroidCanvas) {
        super.onDraw(canvas)

        // Caja visual de firma SOLO en la app
        val padding = 16f
        val left = padding
        val top = padding
        val right = width - padding
        val bottom = height - padding

        canvas.drawRect(left, top, right, bottom, borderPaint)
        canvas.drawPath(path, paint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val padding = 16f
        val minX = padding
        val minY = padding
        val maxX = width - padding
        val maxY = height - padding

        val x = event.x.coerceIn(minX, maxX)
        val y = event.y.coerceIn(minY, maxY)

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                parent?.requestDisallowInterceptTouchEvent(true)
                path.moveTo(x, y)
                lastX = x
                lastY = y
                hasSignature = true
            }
            MotionEvent.ACTION_MOVE -> {
                parent?.requestDisallowInterceptTouchEvent(true)
                val midX = (lastX + x) / 2f
                val midY = (lastY + y) / 2f
                path.quadTo(lastX, lastY, midX, midY)
                lastX = x
                lastY = y
            }
            MotionEvent.ACTION_UP -> {
                parent?.requestDisallowInterceptTouchEvent(false)
                path.lineTo(x, y)
                onSigned?.invoke()
            }
            MotionEvent.ACTION_CANCEL -> {
                parent?.requestDisallowInterceptTouchEvent(false)
            }
        }

        invalidate()
        return true
    }

    fun clear() {
        path.reset()
        hasSignature = false
        invalidate()
        onCleared?.invoke()
    }

    fun hasSignature(): Boolean = hasSignature

    fun exportSignatureBase64(): String? {
        if (!hasSignature || width <= 0 || height <= 0) return null

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = AndroidCanvas(bitmap)

        // Fondo transparente
        canvas.drawColor(AndroidColor.TRANSPARENT, android.graphics.PorterDuff.Mode.CLEAR)

        // La caja NO se exporta al PDF, solo la firma
        canvas.drawPath(path, paint)

        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        val bytes = outputStream.toByteArray()

        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }
}