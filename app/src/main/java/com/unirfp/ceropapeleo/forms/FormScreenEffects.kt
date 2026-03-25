@file:OptIn(ExperimentalFoundationApi::class)

package com.unirfp.ceropapeleo.forms

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.unirfp.ceropapeleo.forms.components.SignaturePadView
import com.unirfp.ceropapeleo.forms.validation.FormValidationResult
import com.unirfp.ceropapeleo.model.CertificateType
import com.unirfp.ceropapeleo.model.Deceased
import com.unirfp.ceropapeleo.model.GenerateRequest
import com.unirfp.ceropapeleo.forms.sections.DeceasedSection
import com.unirfp.ceropapeleo.forms.utils.sanitizeLetters

@Composable
fun handleRealtimeValidation(
    viewModel: GenerateFormViewModel,
    formData: GenerateRequest,
    signaturePadView: SignaturePadView?,
    shouldValidate: Boolean
) {
    LaunchedEffect(formData, signaturePadView, shouldValidate) {
        if (!shouldValidate) return@LaunchedEffect

        viewModel.validate(
            hasSignature = signaturePadView?.hasSignature() == true
        )
    }
}

@Composable
fun handleScrollToFirstError(
    submitAttempt: Int,
    validationSafe: FormValidationResult,
    formData: GenerateRequest,
    signaturePadView: SignaturePadView?,
    requesters: FormRequesters
) {
    LaunchedEffect(submitAttempt, validationSafe, formData.certificateType) {
        if (submitAttempt == 0) return@LaunchedEffect
        if (validationSafe.isFormValid) return@LaunchedEffect

        when {
            // =====================================================
            // 1. BLOQUE COMÚN
            // =====================================================
            formData.applicant.name.isBlank() ->
                requesters.name.bringIntoView()

            formData.applicant.firstSurname.isBlank() ->
                requesters.firstSurname.bringIntoView()

            formData.applicant.documentId.isBlank() ->
                requesters.documentId.bringIntoView()

            formData.applicant.address.street.isBlank() ->
                requesters.street.bringIntoView()

            formData.applicant.address.postalCode.isBlank() || !validationSafe.isPostalCodeValid ->
                requesters.postalCode.bringIntoView()

            formData.applicant.address.city.isBlank() ->
                requesters.city.bringIntoView()

            formData.applicant.address.province.isBlank() ->
                requesters.province.bringIntoView()

            formData.applicant.address.country.isBlank() ->
                requesters.country.bringIntoView()

            formData.applicant.contact.email.isNotBlank() && !validationSafe.isEmailValid ->
                requesters.email.bringIntoView()

            // =====================================================
            // 2. BLOQUE ANTECEDENTES PENALES (17)
            // =====================================================
            formData.certificateType == CertificateType.CRIMINAL_RECORDS &&
                    formData.criminalRecordsDetails.subjectDocumentId.isBlank() ->
                requesters.criminalDocument.bringIntoView()

            formData.certificateType == CertificateType.CRIMINAL_RECORDS &&
                    formData.criminalRecordsDetails.subjectFirstSurnameOrBusinessName.isBlank() ->
                requesters.criminalFirstSurnameOrBusiness.bringIntoView()

            formData.certificateType == CertificateType.CRIMINAL_RECORDS &&
                    formData.criminalRecordsDetails.subjectName.isBlank() ->
                requesters.criminalName.bringIntoView()

            formData.certificateType == CertificateType.CRIMINAL_RECORDS &&
                    formData.criminalRecordsDetails.purpose.isBlank() ->
                requesters.criminalPurpose.bringIntoView()

            // =====================================================
            // 3. BLOQUE FALLECIDO (18 y 19)
            // =====================================================
            formData.certificateType != CertificateType.CRIMINAL_RECORDS &&
                    formData.deathRelatedDetails.deceased.name.isBlank() ->
                requesters.deceasedName.bringIntoView()

            formData.certificateType != CertificateType.CRIMINAL_RECORDS &&
                    formData.deathRelatedDetails.deceased.firstSurname.isBlank() ->
                requesters.deceasedFirstSurname.bringIntoView()

            formData.certificateType != CertificateType.CRIMINAL_RECORDS &&
                    (
                            formData.deathRelatedDetails.deceased.deathDate.isBlank() ||
                                    validationSafe.deathDateError != null
                            ) ->
                requesters.deathDate.bringIntoView()

            // =====================================================
            // 4. FIRMA
            // =====================================================
            formData.signature.place.isBlank() ->
                requesters.signaturePlace.bringIntoView()

            formData.signature.date.isBlank() || validationSafe.signatureDateError != null ->
                requesters.signature.bringIntoView()

            signaturePadView?.hasSignature() != true ->
                requesters.signature.bringIntoView()
        }
    }
}

@Composable
fun handleSubmitError(
    submitError: String?,
    context: android.content.Context,
    onConsumed: () -> Unit,
    onShowErrors: () -> Unit
) {
    LaunchedEffect(submitError) {
        submitError?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            onConsumed()
            onShowErrors()
        }
    }
}

@Composable
fun handleGeneratedPdf(
    generatedPdfUri: android.net.Uri?,
    context: android.content.Context,
    onConsumed: () -> Unit
) {
    LaunchedEffect(generatedPdfUri) {
        generatedPdfUri?.let { uri ->
            Toast.makeText(
                context,
                "PDF guardado en Descargas",
                Toast.LENGTH_LONG
            ).show()

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            }

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

            onConsumed()
        }
    }
}

@Composable
fun deceasedCertificateSection(
    formData: GenerateRequest,
    showErrors: Boolean,
    birthDateTouched: Boolean,
    deathDateTouched: Boolean,
    birthDateError: String?,
    deathDateError: String?,
    todayMillis: Long,
    requesters: FormRequesters,
    onDeceasedChange: (Deceased) -> Unit,
    onBirthDateTouched: () -> Unit,
    onDeathDateTouched: () -> Unit
) {
    DeceasedSection(
        deceased = formData.deathRelatedDetails.deceased,
        showErrors = showErrors,
        birthDateTouchedOrShowErrors = (birthDateTouched || showErrors),
        deathDateTouchedOrShowErrors = (deathDateTouched || showErrors),
        birthDateError = birthDateError,
        deathDateError = deathDateError,
        todayMillis = todayMillis,
        deceasedNameRequester = requesters.deceasedName,
        deceasedFirstSurnameRequester = requesters.deceasedFirstSurname,
        deathDateRequester = requesters.deathDate,
        onDeceasedChange = onDeceasedChange,
        onBirthDateTouched = onBirthDateTouched,
        onDeathDateTouched = onDeathDateTouched,
        sanitizeLetters = { value, max -> value.sanitizeLetters(max) }
    )
}

fun certificateTitle(type: CertificateType): String {
    return when (type) {
        CertificateType.CRIMINAL_RECORDS -> "Formulario 790 - Antecedentes Penales"
        CertificateType.LAST_WILL -> "Formulario 790 - Últimas Voluntades"
        CertificateType.LIFE_INSURANCE -> "Formulario 790 - Cobertura de Fallecimiento"
    }
}