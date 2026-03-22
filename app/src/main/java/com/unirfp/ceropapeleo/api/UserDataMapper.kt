package com.unirfp.ceropapeleo.api

import com.unirfp.ceropapeleo.model.GenerateRequest

object UserDataMapper {

    fun toFlatMap(request: GenerateRequest): Map<String, String> {
        return mapOf(
            // Solicitante
            "name" to request.applicant.name,
            "surname1" to request.applicant.firstSurname,
            "surname2" to request.applicant.secondSurname,
            "documentId" to request.applicant.documentId,

            // Dirección
            "street" to request.applicant.address.street,
            "number" to request.applicant.address.number,
            "staircase" to request.applicant.address.staircase,
            "floor" to request.applicant.address.floor,
            "door" to request.applicant.address.door,
            "postalCode" to request.applicant.address.postalCode,
            "city" to request.applicant.address.city,
            "province" to request.applicant.address.province,
            "country" to request.applicant.address.country,

            // Contacto
            "mobilePhone" to request.applicant.contact.mobilePhone,
            "email" to request.applicant.contact.email,

            // Destino
            "destinationCountry" to request.destination.country,
            "authorityOrEntity" to request.destination.authorityOrEntity,

            // Fallecido
            "deceasedDocumentId" to request.deathRelatedDetails.deceased.documentId,
            "deceasedSurname1" to request.deathRelatedDetails.deceased.firstSurname,
            "deceasedSurname2" to request.deathRelatedDetails.deceased.secondSurname,
            "deceasedName" to request.deathRelatedDetails.deceased.name,
            "birthDate" to request.deathRelatedDetails.deceased.birthDate,
            "birthCity" to request.deathRelatedDetails.deceased.birthCity,
            "deathDate" to request.deathRelatedDetails.deceased.deathDate,
            "deathCity" to request.deathRelatedDetails.deceased.deathCity,

            // Últimas voluntades
            "willDate" to request.deathRelatedDetails.lastWillExtra.willDate,
            "notary" to request.deathRelatedDetails.lastWillExtra.notary,
            "grantPlace" to request.deathRelatedDetails.lastWillExtra.grantPlace,
            "spousesFullName" to request.deathRelatedDetails.lastWillExtra.spousesFullName,

            // Firma
            "signaturePlace" to request.signature.place,
            "signatureDate" to request.signature.date,
            "postalDeliveryAuthorized" to request.signature.postalDeliveryAuthorized.toString(),

            // Pago
            "amountEur" to request.payment.amountEur.toString(),
            "paymentMethod" to request.payment.paymentMethod,
            "bankEnt" to request.payment.bankEnt,
            "bankOff" to request.payment.bankOff,
            "bankDC" to request.payment.bankDC,
            "bankAcc" to request.payment.bankAcc,

            // Tipo de certificado
            "certificateType" to request.certificateType
        ).filterValues { it.isNotBlank() }
    }
}