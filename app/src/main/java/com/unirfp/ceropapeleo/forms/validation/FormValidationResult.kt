package com.unirfp.ceropapeleo.forms.validation

data class FormValidationResult(
    val birthDateError: String?,
    val deathDateError: String?,
    val willDateError: String?,
    val signatureDateError: String?,
    val isEmailValid: Boolean,
    val isPostalCodeValid: Boolean,
    val isBankDataValid: Boolean,
    val isFormValid: Boolean
)