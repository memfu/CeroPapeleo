package com.unirfp.ceropapeleo.forms.state

import android.net.Uri
import com.unirfp.ceropapeleo.forms.validation.FormValidationResult
import com.unirfp.ceropapeleo.model.GenerateRequest

data class GenerateFormUiState(
    val form: GenerateRequest = GenerateRequest(),
    val validation: FormValidationResult? = null,

    val isSubmitting: Boolean = false,
    val submitError: String? = null,
    val generatedPdfUri: Uri? = null,

    val basePdfPath: String? = null,
    val basePdfRequestId: String? = null,
    val basePdfDownloadId: Long? = null,
    val isDownloadingBasePdf: Boolean = false,
    val basePdfDownloadError: String? = null
)