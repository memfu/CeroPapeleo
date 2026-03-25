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
import com.unirfp.ceropapeleo.forms.sections.LastWillExtraSection
import com.unirfp.ceropapeleo.forms.sections.SubmitButtonsSection
import com.unirfp.ceropapeleo.forms.utils.sanitizeLetters
import com.unirfp.ceropapeleo.forms.validation.FormValidator
import com.unirfp.ceropapeleo.model.CertificateType
import com.unirfp.ceropapeleo.forms.rememberFormRequesters
import com.unirfp.ceropapeleo.forms.utils.rememberTodayMillis

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CertificateDetailsScreen(
    navController: NavController
) {
    // ---------------------------------------------------------
    // 1) Estado principal
    // ---------------------------------------------------------
    val viewModel: GenerateFormViewModel = viewModel()
    val uiState = viewModel.uiState
    val formData = uiState.form

    // ---------------------------------------------------------
    // 2) Estado local de UI
    // ---------------------------------------------------------
    var showErrors by remember { mutableStateOf(false) }
    var submitAttempt by remember { mutableIntStateOf(0) }
    var birthDateTouched by remember { mutableStateOf(false) }
    var deathDateTouched by remember { mutableStateOf(false) }
    var willDateTouched by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()
    val requesters = rememberFormRequesters()
    val context = LocalContext.current
    val repository = remember { PdfRepository() }

    // En esta pantalla no se redibuja la firma, pero necesitamos saber
    // si existe para validar y enviar.
    var signaturePadView by remember { mutableStateOf<SignaturePadView?>(null) }

    val todayMillis = rememberTodayMillis()

    // ---------------------------------------------------------
    // 3) Validación
    // ---------------------------------------------------------
    val validationSafe = uiState.validation
        ?: FormValidator.validate(
            formData = formData,
            hasSignature = formData.signature.imageBase64.isNotBlank() ||
                    signaturePadView?.hasSignature() == true
        )

    val birthDateError = validationSafe.birthDateError
    val deathDateError = validationSafe.deathDateError
    val willDateError = validationSafe.willDateError

    // ---------------------------------------------------------
    // 4) Side effects
    // ---------------------------------------------------------
    handleRealtimeValidation(
        viewModel = viewModel,
        formData = formData,
        signaturePadView = signaturePadView,
        shouldValidate = showErrors ||
                birthDateTouched ||
                deathDateTouched ||
                willDateTouched
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

    handleGeneratedPdf(
        generatedPdfUri = uiState.generatedPdfUri,
        context = context,
        onConsumed = { viewModel.clearGeneratedPdfUri() }
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
                "Complete ahora los datos específicos del certificado",
                fontSize = 14.sp
            )

            when (formData.certificateType) {
                CertificateType.CRIMINAL_RECORDS -> {
                    Text("Certificado 17 - Antecedentes Penales (pendiente)")
                }

                CertificateType.LAST_WILL -> {
                    deceasedCertificateSection(
                        formData = formData,
                        showErrors = showErrors,
                        birthDateTouched = birthDateTouched,
                        deathDateTouched = deathDateTouched,
                        birthDateError = birthDateError,
                        deathDateError = deathDateError,
                        todayMillis = todayMillis,
                        requesters = requesters,
                        onDeceasedChange = { newDeceased ->
                            viewModel.updateDeceased(newDeceased)
                        },
                        onBirthDateTouched = { birthDateTouched = true },
                        onDeathDateTouched = { deathDateTouched = true }
                    )

                    LastWillExtraSection(
                        lastWillExtra = formData.deathRelatedDetails.lastWillExtra,
                        willDateTouchedOrShowErrors = (willDateTouched || showErrors),
                        willDateError = willDateError,
                        todayMillis = todayMillis,
                        onLastWillExtraChange = { newLastWillExtra ->
                            viewModel.updateLastWillExtra(newLastWillExtra)
                        },
                        onWillDateTouched = { willDateTouched = true },
                        sanitizeLetters = { value, max -> value.sanitizeLetters(max) }
                    )
                }

                CertificateType.LIFE_INSURANCE -> {
                    deceasedCertificateSection(
                        formData = formData,
                        showErrors = showErrors,
                        birthDateTouched = birthDateTouched,
                        deathDateTouched = deathDateTouched,
                        birthDateError = birthDateError,
                        deathDateError = deathDateError,
                        todayMillis = todayMillis,
                        requesters = requesters,
                        onDeceasedChange = { newDeceased ->
                            viewModel.updateDeceased(newDeceased)
                        },
                        onBirthDateTouched = { birthDateTouched = true },
                        onDeathDateTouched = { deathDateTouched = true }
                    )

                    Text("Certificado 19 - Seguros de cobertura de fallecimiento (pendiente)")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            SubmitButtonsSection(
                navController = navController,
                isSubmitting = uiState.isSubmitting,
                onSubmit = {
                    showErrors = true
                    submitAttempt++

                    val signatureBase64 = formData.signature.imageBase64

                    viewModel.submit(
                        context = context,
                        repository = repository,
                        signatureBase64 = signatureBase64,
                        hasSignature = signatureBase64.isNotBlank()
                    )
                }
            )
        }
    }
}