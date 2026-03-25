package com.unirfp.ceropapeleo.forms

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unirfp.ceropapeleo.api.PdfRepository
import com.unirfp.ceropapeleo.api.UserDataMapper
import com.unirfp.ceropapeleo.forms.state.GenerateFormUiState
import com.unirfp.ceropapeleo.forms.validation.FormValidator
import com.unirfp.ceropapeleo.model.Address
import com.unirfp.ceropapeleo.model.Applicant
import com.unirfp.ceropapeleo.model.CertificateType
import com.unirfp.ceropapeleo.model.Contact
import com.unirfp.ceropapeleo.model.CriminalRecordsDetails
import com.unirfp.ceropapeleo.model.Deceased
import com.unirfp.ceropapeleo.model.Destination
import com.unirfp.ceropapeleo.model.GenerateRequest
import com.unirfp.ceropapeleo.model.LastWillExtra
import com.unirfp.ceropapeleo.model.Payment
import com.unirfp.ceropapeleo.model.Signature
import com.unirfp.ceropapeleo.utils.DownloadUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class GenerateFormViewModel : ViewModel() {

    var uiState by mutableStateOf(GenerateFormUiState())
        private set

    fun startNewForm(certificateCode: String) {
        val type = when (certificateCode) {
            "17" -> CertificateType.CRIMINAL_RECORDS
            "18" -> CertificateType.LAST_WILL
            "19" -> CertificateType.LIFE_INSURANCE
            else -> CertificateType.LAST_WILL
        }

        uiState = GenerateFormUiState(
            form = GenerateRequest(
                certificateType = type
            ),
            basePdfPath = null,
            basePdfRequestId = null,
            basePdfDownloadId = null,
            isDownloadingBasePdf = false,
            basePdfDownloadError = null
        )
    }

    fun initializeCertificateType(certificateCode: String) {
        val type = when (certificateCode) {
            "17" -> CertificateType.CRIMINAL_RECORDS
            "18" -> CertificateType.LAST_WILL
            "19" -> CertificateType.LIFE_INSURANCE
            else -> CertificateType.LAST_WILL
        }

        if (uiState.form.certificateType != type) {
            updateForm(
                uiState.form.copy(certificateType = type)
            )
        }
    }

    fun setBasePdfDownloadState(
        isDownloading: Boolean,
        error: String? = null
    ) {
        uiState = uiState.copy(
            isDownloadingBasePdf = isDownloading,
            basePdfDownloadError = error
        )
    }

    fun setBasePdfInfo(
        requestId: String,
        path: String
    ) {
        uiState = uiState.copy(
            basePdfRequestId = requestId,
            basePdfPath = path,
            basePdfDownloadId = null,
            isDownloadingBasePdf = false,
            basePdfDownloadError = null
        )
    }

    fun setBasePdfDownloadId(downloadId: Long?) {
        uiState = uiState.copy(basePdfDownloadId = downloadId)
    }

    fun setBasePdfPath(path: String?) {
        uiState = uiState.copy(
            basePdfPath = path,
            basePdfDownloadId = null
        )
    }

    fun clearBasePdfDownloadError() {
        uiState = uiState.copy(basePdfDownloadError = null)
    }

    fun updateApplicant(newApplicant: Applicant) {
        updateForm(uiState.form.copy(applicant = newApplicant))
    }

    fun updateContact(newContact: Contact) {
        updateForm(
            uiState.form.copy(
                applicant = uiState.form.applicant.copy(
                    contact = newContact
                )
            )
        )
    }

    fun updateAddress(newAddress: Address) {
        updateForm(
            uiState.form.copy(
                applicant = uiState.form.applicant.copy(
                    address = newAddress
                )
            )
        )
    }

    fun updateDestination(newDestination: Destination) {
        updateForm(uiState.form.copy(destination = newDestination))
    }

    fun updateDeceased(newDeceased: Deceased) {
        updateForm(
            uiState.form.copy(
                deathRelatedDetails = uiState.form.deathRelatedDetails.copy(
                    deceased = newDeceased
                )
            )
        )
    }

    fun updateLastWillExtra(newLastWillExtra: LastWillExtra) {
        updateForm(
            uiState.form.copy(
                deathRelatedDetails = uiState.form.deathRelatedDetails.copy(
                    lastWillExtra = newLastWillExtra
                )
            )
        )
    }

    fun updateCriminalRecordsDetails(newDetails: CriminalRecordsDetails) {
        updateForm(
            uiState.form.copy(
                criminalRecordsDetails = newDetails
            )
        )
    }

    fun updateSignature(newSignature: Signature) {
        updateForm(uiState.form.copy(signature = newSignature))
    }

    fun updatePayment(newPayment: Payment) {
        updateForm(uiState.form.copy(payment = newPayment))
    }

    fun updateCertificateType(newType: CertificateType) {
        updateForm(
            uiState.form.copy(
                certificateType = newType
            )
        )
    }

    fun validate(hasSignature: Boolean) {
        val result = FormValidator.validate(
            formData = uiState.form,
            hasSignature = hasSignature
        )
        uiState = uiState.copy(validation = result)
    }

    fun clearSubmitError() {
        uiState = uiState.copy(submitError = null)
    }

    fun clearGeneratedPdfUri() {
        uiState = uiState.copy(generatedPdfUri = null)
    }

    fun submit(
        context: Context,
        repository: PdfRepository,
        signatureBase64: String,
        hasSignature: Boolean
    ) {
        val validationResult = FormValidator.validate(
            formData = uiState.form,
            hasSignature = hasSignature
        )

        uiState = uiState.copy(validation = validationResult)

        if (!validationResult.isFormValid) {
            uiState = uiState.copy(
                submitError = "Hay errores en el formulario. Revise los campos marcados en rojo"
            )
            return
        }

        val basePdfPath = uiState.basePdfPath
        if (basePdfPath.isNullOrBlank()) {
            uiState = uiState.copy(
                submitError = "No se ha descargado un PDF oficial para esta solicitud"
            )
            return
        }

        viewModelScope.launch {
            uiState = uiState.copy(
                isSubmitting = true,
                submitError = null,
                generatedPdfUri = null
            )

            try {
                val pdfFile = File(basePdfPath)

                if (!pdfFile.exists()) {
                    uiState = uiState.copy(
                        isSubmitting = false,
                        submitError = "No se encontró el PDF oficial asociado a esta solicitud"
                    )
                    return@launch
                }

                val updatedFormData = uiState.form.copy(
                    signature = uiState.form.signature.copy(
                        imageBase64 = signatureBase64
                    )
                )

                val dataMap = UserDataMapper.toFlatMap(updatedFormData)

                val responseBody = withContext(Dispatchers.IO) {
                    repository.uploadAndFillPdf(
                        pdfFile,
                        dataMap,
                        signatureBase64
                    )
                }

                if (responseBody == null) {
                    uiState = uiState.copy(
                        isSubmitting = false,
                        submitError = "El backend no respondió correctamente"
                    )
                    return@launch
                }

                val savedUriString = DownloadUtils.saveApiPdfToDisk(
                    context = context,
                    responseBody = responseBody,
                    fileName = "Solicitud_Final_${System.currentTimeMillis()}.pdf"
                )

                if (savedUriString == null) {
                    uiState = uiState.copy(
                        isSubmitting = false,
                        submitError = "Error al guardar el PDF"
                    )
                    return@launch
                }

                uiState = uiState.copy(
                    isSubmitting = false,
                    generatedPdfUri = Uri.parse(savedUriString)
                )
            } catch (e: Exception) {
                uiState = uiState.copy(
                    isSubmitting = false,
                    submitError = e.message ?: "Error al generar el PDF"
                )
            }
        }
    }

    private fun updateForm(newForm: GenerateRequest) {
        uiState = uiState.copy(form = newForm)
    }
}