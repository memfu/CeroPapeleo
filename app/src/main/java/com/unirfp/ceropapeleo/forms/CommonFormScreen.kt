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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.unirfp.ceropapeleo.forms.components.drawScrollbar
import com.unirfp.ceropapeleo.forms.sections.AddressSection
import com.unirfp.ceropapeleo.forms.sections.ApplicantSection
import com.unirfp.ceropapeleo.forms.sections.ContactSection
import com.unirfp.ceropapeleo.forms.sections.DestinationSection
import com.unirfp.ceropapeleo.forms.sections.SubmitButtonsSection
import com.unirfp.ceropapeleo.forms.utils.sanitizeAlphanumeric
import com.unirfp.ceropapeleo.forms.utils.sanitizeDigits
import com.unirfp.ceropapeleo.forms.utils.sanitizeLetters
import com.unirfp.ceropapeleo.forms.validation.FormValidator
import com.unirfp.ceropapeleo.utils.DownloadUtils
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommonFormScreen(
    navController: NavController,
    certificateCode: String,
    viewModel: GenerateFormViewModel
) {
    // ---------------------------------------------------------
    // 1) Estado principal
    // ---------------------------------------------------------
    val uiState = viewModel.uiState
    val formData = uiState.form
    val context = LocalContext.current

    // Cuando la WebView haya iniciado una descarga oficial, resolvemos
    // el fichero real y lo guardamos como PDF base de esta sesión.
    LaunchedEffect(uiState.basePdfDownloadId) {
        val downloadId = uiState.basePdfDownloadId ?: return@LaunchedEffect

        repeat(20) {
            val resolvedPath = DownloadUtils.resolveDownloadedFilePath(
                context = context,
                downloadId = downloadId
            )

            if (!resolvedPath.isNullOrBlank()) {
                viewModel.setBasePdfPath(resolvedPath)
                return@LaunchedEffect
            }

            delay(500)
        }
    }

    // ---------------------------------------------------------
    // 2) Estado local de UI
    // ---------------------------------------------------------
    var showErrors by remember { mutableStateOf(false) }
    var submitAttempt by remember { mutableIntStateOf(0) }
    var emailTouched by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()
    val requesters = rememberFormRequesters()

    // ---------------------------------------------------------
    // 3) Validación
    // ---------------------------------------------------------
    val validationSafe = uiState.validation
        ?: FormValidator.validate(
            formData = formData,
            hasSignature = true
        )

    val isEmailValid = validationSafe.isEmailValid
    val isPostalCodeValid = validationSafe.isPostalCodeValid

    val postalCode = formData.applicant.address.postalCode
    val countryNormalized = formData.applicant.address.country.trim().lowercase()

    // ---------------------------------------------------------
    // 4) Side effects
    // ---------------------------------------------------------
    handleRealtimeValidation(
        viewModel = viewModel,
        formData = formData,
        signaturePadView = null,
        shouldValidate = showErrors || emailTouched
    )

    handleScrollToFirstError(
        submitAttempt = submitAttempt,
        validationSafe = validationSafe,
        formData = formData,
        signaturePadView = null,
        requesters = requesters
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

            Spacer(modifier = Modifier.height(16.dp))

            SubmitButtonsSection(
                navController = navController,
                isSubmitting = false,
                primaryButtonText = "CONTINUAR",
                secondaryButtonText = "VOLVER",
                onSecondaryClick = { navController.popBackStack() },
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
                        validationSafe.isEmailValid &&
                        validationSafe.isPostalCodeValid &&
                        !uiState.basePdfPath.isNullOrBlank()
                    ) {
                        navController.navigate("certificate_details/$certificateCode")
                    }
                }
            )
        }
    }
}