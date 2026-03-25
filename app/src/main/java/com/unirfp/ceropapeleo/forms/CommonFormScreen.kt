@file:OptIn(ExperimentalFoundationApi::class)

package com.unirfp.ceropapeleo.forms

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.unirfp.ceropapeleo.api.PdfRepository
import com.unirfp.ceropapeleo.forms.components.SignaturePadView
import com.unirfp.ceropapeleo.forms.components.drawScrollbar
import com.unirfp.ceropapeleo.forms.sections.AddressSection
import com.unirfp.ceropapeleo.forms.sections.ApplicantSection
import com.unirfp.ceropapeleo.forms.sections.ContactSection
import com.unirfp.ceropapeleo.forms.sections.DestinationSection
import com.unirfp.ceropapeleo.forms.sections.PaymentSection
import com.unirfp.ceropapeleo.forms.sections.SignatureDateSection
import com.unirfp.ceropapeleo.forms.sections.SignatureSection
import com.unirfp.ceropapeleo.forms.sections.SubmitButtonsSection
import com.unirfp.ceropapeleo.forms.utils.rememberOneYearFromTodayMillis
import com.unirfp.ceropapeleo.forms.utils.sanitizeAlphanumeric
import com.unirfp.ceropapeleo.forms.utils.sanitizeDigits
import com.unirfp.ceropapeleo.forms.utils.sanitizeLetters
import com.unirfp.ceropapeleo.forms.validation.FormValidator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommonFormScreen(
    navController: NavController,
    certificateCode: String
) {
    // ---------------------------------------------------------
    // 1) Estado principal
    // ---------------------------------------------------------
    val viewModel: GenerateFormViewModel = viewModel()
    val uiState = viewModel.uiState
    val formData = uiState.form

    // Inicializa el tipo de certificado al entrar
    androidx.compose.runtime.LaunchedEffect(certificateCode) {
        viewModel.initializeCertificateType(certificateCode)
    }

    // ---------------------------------------------------------
    // 2) Estado local de UI
    // ---------------------------------------------------------
    var showErrors by remember { mutableStateOf(false) }
    var submitAttempt by remember { mutableIntStateOf(0) }
    var emailTouched by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()
    val requesters = rememberFormRequesters()

    val context = LocalContext.current
    remember { PdfRepository() } // de momento no se usa aquí, pero lo dejamos fuera
    var signaturePadView by remember { mutableStateOf<SignaturePadView?>(null) }

    val oneYearFromTodayMillis = rememberOneYearFromTodayMillis()

    // ---------------------------------------------------------
    // 3) Validación
    // ---------------------------------------------------------
    val validationSafe = uiState.validation
        ?: FormValidator.validate(
            formData = formData,
            hasSignature = signaturePadView?.hasSignature() == true
        )

    val signatureDateError = validationSafe.signatureDateError
    val isEmailValid = validationSafe.isEmailValid
    val isPostalCodeValid = validationSafe.isPostalCodeValid
    val isSignatureDateValid = signatureDateError == null

    val postalCode = formData.applicant.address.postalCode
    val countryNormalized = formData.applicant.address.country.trim().lowercase()

    // ---------------------------------------------------------
    // 4) Side effects
    // ---------------------------------------------------------
    handleRealtimeValidation(
        viewModel = viewModel,
        formData = formData,
        signaturePadView = signaturePadView,
        shouldValidate = showErrors || emailTouched
    )

    handleScrollToFirstError(
        submitAttempt = submitAttempt,
        validationSafe = validationSafe,
        formData = formData,
        signaturePadView = signaturePadView,
        requesters = requesters
    )

    handleSubmitError(
        submitError = uiState.submitError,
        context = context,
        onConsumed = { viewModel.clearSubmitError() },
        onShowErrors = { showErrors = true }
    )

    // ---------------------------------------------------------
    // 5) UI
    // ---------------------------------------------------------
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(certificateTitle(formData.certificateType)) }
            )
        }
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
            Text(
                "Complete primero los datos comunes del formulario",
                fontSize = 14.sp
            )

            ApplicantSection(
                applicant = formData.applicant,
                showErrors = showErrors,
                nameRequester = requesters.name,
                firstSurnameRequester = requesters.firstSurname,
                documentIdRequester = requesters.documentId,
                onApplicantChange = { newApplicant ->
                    viewModel.updateApplicant(newApplicant)
                },
                sanitizeLetters = { value, max -> value.sanitizeLetters(max) }
            )

            ContactSection(
                contact = formData.applicant.contact,
                email = formData.applicant.contact.email,
                emailTouchedOrShowErrors = (emailTouched || showErrors),
                isEmailValid = isEmailValid,
                emailRequester = requesters.email,
                onContactChange = { newContact ->
                    viewModel.updateContact(newContact)
                },
                onEmailBlur = { emailTouched = true },
                sanitizeDigits = { value, max -> value.sanitizeDigits(max) }
            )

            AddressSection(
                address = formData.applicant.address,
                showErrors = showErrors,
                postalCode = postalCode,
                countryNormalized = countryNormalized,
                isPostalCodeValid = isPostalCodeValid,
                streetRequester = requesters.street,
                postalCodeRequester = requesters.postalCode,
                cityRequester = requesters.city,
                provinceRequester = requesters.province,
                countryRequester = requesters.country,
                onAddressChange = { newAddress ->
                    viewModel.updateAddress(newAddress)
                },
                sanitizeLetters = { value, max -> value.sanitizeLetters(max) },
                sanitizeDigits = { value, max -> value.sanitizeDigits(max) },
                sanitizeAlphanumeric = { value, max -> value.sanitizeAlphanumeric(max) }
            )

            DestinationSection(
                destination = formData.destination,
                onDestinationChange = { newDestination ->
                    viewModel.updateDestination(newDestination)
                },
                sanitizeLetters = { value, max -> value.sanitizeLetters(max) }
            )

            SignatureSection(
                signature = formData.signature,
                onSignatureChange = { newSignature ->
                    viewModel.updateSignature(newSignature)
                },
                onPadReady = { view ->
                    signaturePadView = view
                },
                onSignatureCleared = {
                    viewModel.updateSignature(
                        formData.signature.copy(imageBase64 = "")
                    )
                }
            )

            SignatureDateSection(
                signature = formData.signature,
                showErrors = showErrors,
                isSignatureDateValid = isSignatureDateValid,
                signatureDateError = signatureDateError,
                signatureRequester = requesters.signature,
                signaturePlaceRequester = requesters.signaturePlace,
                onSignatureChange = { newSignature ->
                    viewModel.updateSignature(newSignature)
                },
                sanitizeLetters = { value, max -> value.sanitizeLetters(max) },
                oneYearFromTodayMillis = oneYearFromTodayMillis
            )

            PaymentSection(
                payment = formData.payment,
                showErrors = showErrors,
                onPaymentChange = { newPayment ->
                    viewModel.updatePayment(newPayment)
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            SubmitButtonsSection(
                navController = navController,
                isSubmitting = false,
                onSubmit = {
                    showErrors = true
                    submitAttempt++

                    if (
                        formData.applicant.name.isNotBlank() &&
                        formData.applicant.firstSurname.isNotBlank() &&
                        formData.applicant.documentId.isNotBlank() &&
                        formData.applicant.address.street.isNotBlank() &&
                        formData.applicant.address.city.isNotBlank() &&
                        formData.applicant.address.province.isNotBlank() &&
                        formData.applicant.address.country.isNotBlank() &&
                        formData.signature.place.isNotBlank() &&
                        validationSafe.signatureDateError == null &&
                        validationSafe.isEmailValid &&
                        validationSafe.isPostalCodeValid &&
                        validationSafe.isBankDataValid &&
                        signaturePadView?.hasSignature() == true
                    ) {
                        navController.navigate("certificate_details")
                    }
                }
            )
        }
    }
}