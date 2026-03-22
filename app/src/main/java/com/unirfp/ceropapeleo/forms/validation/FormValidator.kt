package com.unirfp.ceropapeleo.forms.validation

import com.unirfp.ceropapeleo.forms.utils.isValidSpanishDate
import com.unirfp.ceropapeleo.forms.utils.toLocalDateOrNull
import com.unirfp.ceropapeleo.model.GenerateRequest

object FormValidator {

    fun validate(
        formData: GenerateRequest,
        hasSignature: Boolean
    ): FormValidationResult {
        val email = formData.applicant.contact.email
        val postalCode = formData.applicant.address.postalCode
        val countryNormalized = formData.applicant.address.country.trim().lowercase()

        val isEmailValid = email.isBlank() ||
                android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()

        val isPostalCodeValid = when (countryNormalized) {
            "españa", "spain" -> postalCode.length == 5
            else -> postalCode.isNotBlank()
        }

        val birthDateValue = formData.deathRelatedDetails.deceased.birthDate
        val deathDateValue = formData.deathRelatedDetails.deceased.deathDate
        val willDateValue = formData.deathRelatedDetails.lastWillExtra.willDate
        val signatureDateValue = formData.signature.date

        val birthDateParsed = birthDateValue.toLocalDateOrNull()
        val deathDateParsed = deathDateValue.toLocalDateOrNull()
        val willDateParsed = willDateValue.toLocalDateOrNull()
        val signatureDateParsed = signatureDateValue.toLocalDateOrNull()

        val isBirthBeforeDeath =
            birthDateParsed == null || deathDateParsed == null || birthDateParsed.isBefore(deathDateParsed)

        val isWillBeforeDeath =
            willDateParsed == null || deathDateParsed == null || willDateParsed.isBefore(deathDateParsed)

        val isBirthBeforeWill =
            birthDateParsed == null || willDateParsed == null || birthDateParsed.isBefore(willDateParsed)

        val isSignatureAfterDeath =
            signatureDateParsed == null || deathDateParsed == null ||
                    signatureDateParsed.isAfter(deathDateParsed) ||
                    signatureDateParsed.isEqual(deathDateParsed)

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

        val signatureDateError = when {
            signatureDateValue.isBlank() -> "Campo obligatorio"
            !signatureDateValue.isValidSpanishDate() -> "Fecha de firma inválida"
            !isSignatureAfterDeath -> "La fecha de firma debe ser posterior o igual a la de defunción"
            else -> null
        }

        val isBankDataValid =
            formData.payment.paymentMethod == "CASH" ||
                    (
                            formData.payment.bankEnt.length == 4 &&
                                    formData.payment.bankOff.length == 4 &&
                                    formData.payment.bankDC.length == 2 &&
                                    formData.payment.bankAcc.length == 10
                            )

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
                signatureDateError == null &&
                formData.signature.place.isNotBlank() &&
                hasSignature &&
                isPostalCodeValid &&
                isEmailValid &&
                isBankDataValid

        return FormValidationResult(
            birthDateError = birthDateError,
            deathDateError = deathDateError,
            willDateError = willDateError,
            signatureDateError = signatureDateError,
            isEmailValid = isEmailValid,
            isPostalCodeValid = isPostalCodeValid,
            isBankDataValid = isBankDataValid,
            isFormValid = isFormValid
        )
    }
}