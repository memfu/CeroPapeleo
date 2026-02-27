package com.ceropapeleo.backend.logic

import com.ceropapeleo.backend.dto.*

class RequestValidator {
    fun validate(request: GenerateRequest): List<FieldError> {
        val errors = mutableListOf<FieldError>()

        // 1. Validaciones básicas de Applicant
        if (request.applicant.documentId.isBlank()) errors.add(FieldError("applicant.documentId", "Obligatorio"))
        if (request.applicant.name.isBlank()) errors.add(FieldError("applicant.name", "Obligatorio"))
        if (request.applicant.contact.email.isNullOrBlank() && request.applicant.contact.mobilePhone.isNullOrBlank()) {
            errors.add(FieldError("applicant.contact", "Debe incluir email o teléfono móvil"))
        }

        // 2. Pago
        if (request.payment.amountEur <= 0) errors.add(FieldError("payment.amountEur", "Debe ser mayor a 0"))
        if (request.payment.paymentMethod == PaymentMethod.DIRECT_DEBIT && request.payment.customerAccount.isNullOrBlank()) {
            errors.add(FieldError("payment.customerAccount", "Obligatorio para domiciliación"))
        }

        // 3. Firma
        if (request.signature.place.isBlank()) errors.add(FieldError("signature.place", "Obligatorio"))
        if (request.signature.date.isBlank()) errors.add(FieldError("signature.date", "Obligatorio"))

        // 4. Lógica Condicional por CertificateType
        when (request.certificateType) {
            CertificateType.LAST_WILL -> {
                if (request.deathRelatedDetails == null) {
                    errors.add(FieldError("deathRelatedDetails", "Obligatorio para LAST_WILL"))
                }
            }
            CertificateType.DEATH_INSURANCE_CONTRACTS -> {
                if (request.deathRelatedDetails == null) {
                    errors.add(FieldError("deathRelatedDetails", "Obligatorio para seguros de defunción"))
                }
                if (request.deathRelatedDetails?.lastWillExtra != null) {
                    errors.add(FieldError("lastWillExtra", "No debe incluirse para este tipo de certificado"))
                }
            }
            CertificateType.CRIMINAL_RECORD -> {
                errors.add(FieldError("certificateType", "CRIMINAL_RECORD Not implemented yet"))
            }
        }
        return errors
    }
}