package com.unirfp.ceropapeleo.forms

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.ExperimentalFoundationApi
// Composables
import com.unirfp.ceropapeleo.forms.components.drawScrollbar
import com.unirfp.ceropapeleo.forms.components.SignaturePadView
// Secciones
import com.unirfp.ceropapeleo.forms.sections.ApplicantSection
import com.unirfp.ceropapeleo.forms.sections.ContactSection
import com.unirfp.ceropapeleo.forms.sections.AddressSection
import com.unirfp.ceropapeleo.forms.sections.DestinationSection
import com.unirfp.ceropapeleo.forms.sections.DeceasedSection
import com.unirfp.ceropapeleo.forms.sections.LastWillExtraSection
import com.unirfp.ceropapeleo.forms.sections.PaymentSection
import com.unirfp.ceropapeleo.forms.sections.SignatureSection
import com.unirfp.ceropapeleo.forms.sections.SignatureDateSection
// Helpers para validaciones
import com.unirfp.ceropapeleo.forms.utils.sanitizeLetters
import com.unirfp.ceropapeleo.forms.utils.sanitizeDigits
import com.unirfp.ceropapeleo.forms.utils.sanitizeDate
import com.unirfp.ceropapeleo.forms.utils.sanitizeAlphanumeric
import com.unirfp.ceropapeleo.forms.utils.toLocalDateOrNull
import com.unirfp.ceropapeleo.forms.utils.isValidSpanishDate
import com.unirfp.ceropapeleo.forms.validation.FormValidator

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

    // Para poder lanzar la petición:
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val repository = remember { PdfRepository() }
    var signaturePadView by remember { mutableStateOf<SignaturePadView?>(null) }

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

    // Estados "touched" para validar campos al "tocarlos" y no introducir datos válidos
    var birthDateTouched by remember { mutableStateOf(false) }
    var deathDateTouched by remember { mutableStateOf(false) }
    var willDateTouched by remember { mutableStateOf(false) }
    var emailTouched by remember { mutableStateOf(false) }

    val validation = FormValidator.validate(
        formData = formData,
        hasSignature = signaturePadView?.hasSignature() == true
    )

    val birthDateError = validation.birthDateError
    val deathDateError = validation.deathDateError
    val willDateError = validation.willDateError
    val signatureDateError = validation.signatureDateError

    val isEmailValid = validation.isEmailValid
    val isPostalCodeValid = validation.isPostalCodeValid
    val isSignatureDateValid = signatureDateError == null
    val isFormValid = validation.isFormValid

    val deathDateValue = formData.deathRelatedDetails.deceased.deathDate

    val postalCode = formData.applicant.address.postalCode
    val countryNormalized = formData.applicant.address.country.trim().lowercase()

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
            ApplicantSection(
                applicant = formData.applicant,
                showErrors = showErrors,
                nameRequester = nameRequester,
                firstSurnameRequester = firstSurnameRequester,
                documentIdRequester = documentIdRequester,
                onApplicantChange = { newApplicant ->
                    formData = formData.copy(applicant = newApplicant)
                },
                sanitizeLetters = { value, max -> value.sanitizeLetters(max) }
            )

            // --- 2. DATOS DE CONTACTO DEL SOLICITANTE ---
            ContactSection(
                contact = formData.applicant.contact,
                email = email,
                emailTouchedOrShowErrors = (emailTouched || showErrors),
                isEmailValid = isEmailValid,
                emailRequester = emailRequester,
                onContactChange = { newContact ->
                    formData = formData.copy(
                        applicant = formData.applicant.copy(contact = newContact)
                    )
                },
                onEmailBlur = { emailTouched = true },
                sanitizeDigits = { value, max -> value.sanitizeDigits(max) }
            )

            // --- 3. DOMICILIO DEL SOLICITANTE ---
            AddressSection(
                address = formData.applicant.address,
                showErrors = showErrors,
                postalCode = postalCode,
                countryNormalized = countryNormalized,
                isPostalCodeValid = isPostalCodeValid,
                streetRequester = streetRequester,
                postalCodeRequester = postalCodeRequester,
                cityRequester = cityRequester,
                provinceRequester = provinceRequester,
                countryRequester = countryRequester,
                onAddressChange = { newAddress ->
                    formData = formData.copy(
                        applicant = formData.applicant.copy(address = newAddress)
                    )
                },
                sanitizeLetters = { value, max -> value.sanitizeLetters(max) },
                sanitizeDigits = { value, max -> value.sanitizeDigits(max) },
                sanitizeAlphanumeric = { value, max -> value.sanitizeAlphanumeric(max) }
            )

            // --- 3. APARTADO: EFECTOS EN EL EXTRANJERO ---

            DestinationSection(
                destination = formData.destination,
                onDestinationChange = { newDestination ->
                    formData = formData.copy(destination = newDestination)
                },
                sanitizeLetters = { value, max -> value.sanitizeLetters(max) }
            )

            // --- 4. DATOS DEL FALLECIDO ---
            DeceasedSection(
                deceased = formData.deathRelatedDetails.deceased,
                showErrors = showErrors,
                birthDateTouchedOrShowErrors = (birthDateTouched || showErrors),
                deathDateTouchedOrShowErrors = (deathDateTouched || showErrors),
                birthDateError = birthDateError,
                deathDateError = deathDateError,
                todayMillis = todayMillis,
                deceasedNameRequester = deceasedNameRequester,
                deceasedFirstSurnameRequester = deceasedFirstSurnameRequester,
                deathDateRequester = deathDateRequester,
                onDeceasedChange = { newDeceased ->
                    formData = formData.copy(
                        deathRelatedDetails = formData.deathRelatedDetails.copy(
                            deceased = newDeceased
                        )
                    )
                },
                onBirthDateTouched = { birthDateTouched = true },
                onDeathDateTouched = { deathDateTouched = true },
                sanitizeLetters = { value, max -> value.sanitizeLetters(max) }
            )

            // --- 5. DATOS ULTIMAS VOLUNTADES ---
            LastWillExtraSection(
                lastWillExtra = formData.deathRelatedDetails.lastWillExtra,
                willDateTouchedOrShowErrors = (willDateTouched || showErrors),
                willDateError = willDateError,
                todayMillis = todayMillis,
                onLastWillExtraChange = { newLastWillExtra ->
                    formData = formData.copy(
                        deathRelatedDetails = formData.deathRelatedDetails.copy(
                            lastWillExtra = newLastWillExtra
                        )
                    )
                },
                onWillDateTouched = { willDateTouched = true },
                sanitizeLetters = { value, max -> value.sanitizeLetters(max) }
            )

            // --- 6. FIRMA ---
            SignatureSection(
                signature = formData.signature,
                onSignatureChange = { newSignature ->
                    formData = formData.copy(signature = newSignature)
                },
                onPadReady = { view ->
                    signaturePadView = view
                },
                onSignatureCleared = {
                    formData = formData.copy(
                        signature = formData.signature.copy(imageBase64 = "")
                    )
                }
            )

            // --- APARTADO: LUGAR Y FECHA DE LA FIRMA ---
            SignatureDateSection(
                signature = formData.signature,
                showErrors = showErrors,
                isSignatureDateValid = isSignatureDateValid,
                signatureDateError = signatureDateError,
                signatureRequester = signatureRequester,
                signaturePlaceRequester = signaturePlaceRequester,
                onSignatureChange = { newSignature ->
                    formData = formData.copy(signature = newSignature)
                },
                sanitizeLetters = { value, max -> value.sanitizeLetters(max) },
                oneYearFromTodayMillis = oneYearFromTodayMillis
            )

            // --- SECCIÓN INGRESO Y FORMA DE PAGO ---
            PaymentSection(
                payment = formData.payment,
                showErrors = showErrors,
                onPaymentChange = { newPayment ->
                    formData = formData.copy(payment = newPayment)
                }
            )

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