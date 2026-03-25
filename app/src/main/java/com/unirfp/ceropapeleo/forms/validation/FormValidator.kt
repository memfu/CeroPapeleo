package com.unirfp.ceropapeleo.forms.validation

import android.util.Patterns
import com.unirfp.ceropapeleo.forms.utils.isValidSpanishDate
import com.unirfp.ceropapeleo.forms.utils.toLocalDateOrNull
import com.unirfp.ceropapeleo.model.CertificateType
import com.unirfp.ceropapeleo.model.GenerateRequest

public object FormValidator {

    // =========================================================
    // 🟦 ENTRY POINT
    // =========================================================

    fun validate(
        formData: GenerateRequest,
        hasSignature: Boolean
    ): FormValidationResult {

        val common = validateCommon(formData, hasSignature)

        return when (formData.certificateType) {

            // 17 - Antecedentes Penales
            CertificateType.CRIMINAL_RECORDS -> {
                validateCriminalRecords(formData, hasSignature)
            }

            // 18 - Últimas Voluntades
            CertificateType.LAST_WILL -> {
                val specific = validateLastWillSpecific(formData)
                mergeValidation(common, specific)
            }

            // 19 - Seguros de Fallecimiento
            CertificateType.LIFE_INSURANCE -> {
                val specific = validateLifeInsuranceSpecific(formData)
                mergeValidation(common, specific)
            }
        }
    }

    // =========================================================
    // 🟩 VALIDACIÓN COMÚN (todos los certificados)
    // =========================================================

    private fun validateCommon(
        formData: GenerateRequest,
        hasSignature: Boolean
    ): FormValidationResult {

        val isEmailValid = isEmailValid(formData.applicant.contact.email)

        val isPostalCodeValid = isPostalCodeValid(
            postalCode = formData.applicant.address.postalCode,
            country = formData.applicant.address.country
        )

        val signatureDateError =
            validateCommonSignatureDate(formData.signature.date)

        val isBankDataValid = isBankDataValid(formData)

        val isCommonValid =
            formData.applicant.name.isNotBlank() &&
                    formData.applicant.firstSurname.isNotBlank() &&
                    formData.applicant.documentId.isNotBlank() &&
                    formData.applicant.address.street.isNotBlank() &&
                    formData.applicant.address.city.isNotBlank() &&
                    formData.applicant.address.province.isNotBlank() &&
                    formData.applicant.address.country.isNotBlank() &&
                    formData.signature.place.isNotBlank() &&
                    signatureDateError == null &&
                    hasSignature &&
                    isPostalCodeValid &&
                    isEmailValid &&
                    isBankDataValid

        return FormValidationResult(
            birthDateError = null,
            deathDateError = null,
            willDateError = null,
            signatureDateError = signatureDateError,
            isEmailValid = isEmailValid,
            isPostalCodeValid = isPostalCodeValid,
            isBankDataValid = isBankDataValid,
            isFormValid = isCommonValid
        )
    }

    // =========================================================
    // 🟨 17 - ANTECEDENTES PENALES
    // =========================================================

    private fun validateCriminalRecords(
        formData: GenerateRequest,
        hasSignature: Boolean
    ): FormValidationResult {
        val common = validateCommon(formData, hasSignature)

        val birthDateValue = formData.criminalRecordsDetails.birthDate
        val signatureDateValue = formData.signature.date

        val birthDateParsed = birthDateValue.toLocalDateOrNull()
        val signatureDateParsed = signatureDateValue.toLocalDateOrNull()

        val isBirthBeforeSignature =
            birthDateParsed == null || signatureDateParsed == null ||
                    birthDateParsed.isBefore(signatureDateParsed)
        val criminalBirthDateError = when {
            birthDateValue.isBlank() -> null
            !birthDateValue.isValidSpanishDate() -> "Fecha de nacimiento inválida"
            !isBirthBeforeSignature ->
                "La fecha de nacimiento debe ser anterior a la de firma"
            else -> null
        }

        val isSpecificValid =
            formData.criminalRecordsDetails.subjectDocumentId.isNotBlank() &&
                    formData.criminalRecordsDetails.subjectFirstSurnameOrBusinessName.isNotBlank() &&
                    formData.criminalRecordsDetails.subjectName.isNotBlank() &&
                    formData.criminalRecordsDetails.purpose.isNotBlank() &&
                    criminalBirthDateError == null

        return FormValidationResult(
            birthDateError = criminalBirthDateError,
            deathDateError = null,
            willDateError = null,
            signatureDateError = common.signatureDateError,
            isEmailValid = common.isEmailValid,
            isPostalCodeValid = common.isPostalCodeValid,
            isBankDataValid = common.isBankDataValid,
            isFormValid = common.isFormValid && isSpecificValid
        )
    }

    // =========================================================
    // 🟥 18 - ÚLTIMAS VOLUNTADES
    // =========================================================

    private fun validateLastWillSpecific(
        formData: GenerateRequest
    ): FormValidationResult {

        val (birthError, deathError, deceasedValid) =
            validateDeceasedBase(formData)

        val (willError, willValid) =
            validateLastWillExtra(formData, birthError, deathError)

        val signatureDateError =
            validateSignatureAfterDeath(formData)

        val isValid =
            deceasedValid && willValid && signatureDateError == null

        return FormValidationResult(
            birthDateError = birthError,
            deathDateError = deathError,
            willDateError = willError,
            signatureDateError = signatureDateError,
            isEmailValid = true,
            isPostalCodeValid = true,
            isBankDataValid = true,
            isFormValid = isValid
        )
    }

    private fun validateLastWillExtra(
        formData: GenerateRequest,
        birthDateError: String?,
        deathDateError: String?
    ): Pair<String?, Boolean> {

        val birthDateValue = formData.deathRelatedDetails.deceased.birthDate
        val deathDateValue = formData.deathRelatedDetails.deceased.deathDate
        val willDateValue = formData.deathRelatedDetails.lastWillExtra.willDate

        val birthDateParsed = birthDateValue.toLocalDateOrNull()
        val deathDateParsed = deathDateValue.toLocalDateOrNull()
        val willDateParsed = willDateValue.toLocalDateOrNull()

        val isWillBeforeDeath =
            willDateParsed == null || deathDateParsed == null ||
                    willDateParsed.isBefore(deathDateParsed)

        val isBirthBeforeWill =
            birthDateParsed == null || willDateParsed == null ||
                    birthDateParsed.isBefore(willDateParsed)

        val willDateError = when {
            willDateValue.isBlank() -> null
            !willDateValue.isValidSpanishDate() -> "Fecha del testamento inválida"
            !isWillBeforeDeath -> "La fecha del testamento debe ser anterior a la de defunción"
            !isBirthBeforeWill -> "La fecha del testamento debe ser posterior a la de nacimiento"
            else -> null
        }

        val isValid =
            (formData.deathRelatedDetails.lastWillExtra.willDate.isBlank() ||
                    formData.deathRelatedDetails.lastWillExtra.willDate.isValidSpanishDate()) &&
                    willDateError == null &&
                    birthDateError == null &&
                    deathDateError == null

        return Pair(willDateError, isValid)
    }

    // =========================================================
    // 🟪 19 - SEGUROS DE FALLECIMIENTO
    // =========================================================

    private fun validateLifeInsuranceSpecific(
        formData: GenerateRequest
    ): FormValidationResult {

        val (birthError, deathError, deceasedValid) =
            validateDeceasedBase(formData)

        val signatureDateError =
            validateSignatureAfterDeath(formData)

        val isValid =
            deceasedValid && signatureDateError == null

        return FormValidationResult(
            birthDateError = birthError,
            deathDateError = deathError,
            willDateError = null,
            signatureDateError = signatureDateError,
            isEmailValid = true,
            isPostalCodeValid = true,
            isBankDataValid = true,
            isFormValid = isValid
        )
    }

    // =========================================================
    // 🔵 HELPERS COMUNES (reutilizados por varios certificados)
    // =========================================================

    private fun validateDeceasedBase(
        formData: GenerateRequest
    ): Triple<String?, String?, Boolean> {

        val birthDateValue = formData.deathRelatedDetails.deceased.birthDate
        val deathDateValue = formData.deathRelatedDetails.deceased.deathDate

        val birthDateParsed = birthDateValue.toLocalDateOrNull()
        val deathDateParsed = deathDateValue.toLocalDateOrNull()

        val isBirthBeforeDeath =
            birthDateParsed == null || deathDateParsed == null ||
                    birthDateParsed.isBefore(deathDateParsed)

        val birthDateError = when {
            birthDateValue.isBlank() -> null
            !birthDateValue.isValidSpanishDate() -> "Fecha de nacimiento inválida"
            !isBirthBeforeDeath -> "La fecha de nacimiento debe ser anterior a la de defunción"
            else -> null
        }

        val deathDateError = when {
            deathDateValue.isBlank() -> "Campo obligatorio"
            !deathDateValue.isValidSpanishDate() -> "Fecha de defunción inválida"
            !isBirthBeforeDeath -> "La fecha de defunción debe ser posterior a la de nacimiento"
            else -> null
        }

        val isValid =
            formData.deathRelatedDetails.deceased.name.isNotBlank() &&
                    formData.deathRelatedDetails.deceased.firstSurname.isNotBlank() &&
                    formData.deathRelatedDetails.deceased.deathDate.isValidSpanishDate() &&
                    (formData.deathRelatedDetails.deceased.birthDate.isBlank() ||
                            formData.deathRelatedDetails.deceased.birthDate.isValidSpanishDate()) &&
                    birthDateError == null &&
                    deathDateError == null

        return Triple(birthDateError, deathDateError, isValid)
    }

    private fun validateSignatureAfterDeath(
        formData: GenerateRequest
    ): String? {

        val deathDate = formData.deathRelatedDetails.deceased.deathDate
        val signatureDate = formData.signature.date

        val deathParsed = deathDate.toLocalDateOrNull()
        val signatureParsed = signatureDate.toLocalDateOrNull()

        val isValid =
            signatureParsed == null || deathParsed == null ||
                    signatureParsed.isAfter(deathParsed) ||
                    signatureParsed.isEqual(deathParsed)

        return when {
            signatureDate.isBlank() -> "Campo obligatorio"
            !signatureDate.isValidSpanishDate() -> "Fecha de firma inválida"
            !isValid -> "La fecha de firma debe ser posterior o igual a la de defunción"
            else -> null
        }
    }

    private fun isEmailValid(email: String): Boolean {
        return email.isBlank() ||
                Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun isPostalCodeValid(
        postalCode: String,
        country: String
    ): Boolean {
        return when (country.trim().lowercase()) {
            "españa", "spain" -> postalCode.length == 5
            else -> postalCode.isNotBlank()
        }
    }

    private fun validateCommonSignatureDate(
        signatureDateValue: String
    ): String? {
        return when {
            signatureDateValue.isBlank() -> "Campo obligatorio"
            !signatureDateValue.isValidSpanishDate() -> "Fecha de firma inválida"
            else -> null
        }
    }

    private fun isBankDataValid(
        formData: GenerateRequest
    ): Boolean {
        return formData.payment.paymentMethod == "CASH" ||
                (
                        formData.payment.bankEnt.length == 4 &&
                                formData.payment.bankOff.length == 4 &&
                                formData.payment.bankDC.length == 2 &&
                                formData.payment.bankAcc.length == 10
                        )
    }

    private fun mergeValidation(
        common: FormValidationResult,
        specific: FormValidationResult
    ): FormValidationResult {
        return FormValidationResult(
            birthDateError = specific.birthDateError,
            deathDateError = specific.deathDateError,
            willDateError = specific.willDateError,
            signatureDateError = specific.signatureDateError ?: common.signatureDateError,
            isEmailValid = common.isEmailValid,
            isPostalCodeValid = common.isPostalCodeValid,
            isBankDataValid = common.isBankDataValid,
            isFormValid = common.isFormValid && specific.isFormValid
        )
    }
}