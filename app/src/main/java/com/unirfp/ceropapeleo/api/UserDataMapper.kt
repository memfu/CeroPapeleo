package com.unirfp.ceropapeleo.api

import com.unirfp.ceropapeleo.model.CertificateType
import com.unirfp.ceropapeleo.model.GenerateRequest

object UserDataMapper {

    fun toFlatMap(request: GenerateRequest): Map<String, String> {
        return mapOf(
            // =====================================================
            // 1. SOLICITANTE
            // =====================================================
            "name" to request.applicant.name,
            "surname1" to request.applicant.firstSurname,
            "surname2" to request.applicant.secondSurname,
            "documentId" to request.applicant.documentId,

            // =====================================================
            // 2. DIRECCIÓN
            // =====================================================
            "street" to request.applicant.address.street,
            "number" to request.applicant.address.number,
            "staircase" to request.applicant.address.staircase,
            "floor" to request.applicant.address.floor,
            "door" to request.applicant.address.door,
            "postalCode" to request.applicant.address.postalCode,
            "city" to request.applicant.address.city,
            "province" to request.applicant.address.province,
            "country" to request.applicant.address.country,

            // =====================================================
            // 3. CONTACTO
            // =====================================================
            "mobilePhone" to request.applicant.contact.mobilePhone,
            "email" to request.applicant.contact.email,

            // =====================================================
            // 4. DESTINO
            // =====================================================
            "destinationCountry" to request.destination.country,
            "authorityOrEntity" to request.destination.authorityOrEntity,

            // =====================================================
            // 5. ANTECEDENTES PENALES
            // =====================================================
            "criminalSubjectDocumentId" to request.criminalRecordsDetails.subjectDocumentId,
            "criminalSubjectSurname1OrBusinessName" to request.criminalRecordsDetails.subjectFirstSurnameOrBusinessName,
            "criminalSubjectSurname2" to request.criminalRecordsDetails.subjectSecondSurname,
            "criminalSubjectName" to request.criminalRecordsDetails.subjectName,
            "criminalBirthDate" to request.criminalRecordsDetails.birthDate,
            "criminalBirthCity" to request.criminalRecordsDetails.birthCity,
            "criminalBirthProvinceOrCountry" to request.criminalRecordsDetails.birthProvinceOrCountry,
            "criminalNationalityCountry" to request.criminalRecordsDetails.nationalityCountry,
            "criminalFatherName" to request.criminalRecordsDetails.fatherName,
            "criminalMotherName" to request.criminalRecordsDetails.motherName,
            "criminalPurpose" to request.criminalRecordsDetails.purpose,

            // =====================================================
            // 6. FALLECIDO
            // =====================================================
            "deceasedDocumentId" to request.deathRelatedDetails.deceased.documentId,
            "deceasedSurname1" to request.deathRelatedDetails.deceased.firstSurname,
            "deceasedSurname2" to request.deathRelatedDetails.deceased.secondSurname,
            "deceasedName" to request.deathRelatedDetails.deceased.name,
            "birthDate" to request.deathRelatedDetails.deceased.birthDate,
            "birthCity" to request.deathRelatedDetails.deceased.birthCity,
            "deathDate" to request.deathRelatedDetails.deceased.deathDate,
            "deathCity" to request.deathRelatedDetails.deceased.deathCity,

            // =====================================================
            // 7. ÚLTIMAS VOLUNTADES
            // =====================================================
            "willDate" to request.deathRelatedDetails.lastWillExtra.willDate,
            "notary" to request.deathRelatedDetails.lastWillExtra.notary,
            "grantPlace" to request.deathRelatedDetails.lastWillExtra.grantPlace,
            "spousesFullName" to request.deathRelatedDetails.lastWillExtra.spousesFullName,

            // =====================================================
            // 8. FIRMA
            // =====================================================
            "signaturePlace" to request.signature.place,
            "signatureDate" to request.signature.date,
            "postalDeliveryAuthorized" to request.signature.postalDeliveryAuthorized.toString(),

            // =====================================================
            // 9. PAGO
            // =====================================================
            "amountEur" to request.payment.amountEur.toString().replace(".", ","),
            "paymentMethod" to request.payment.paymentMethod,
            "bankEnt" to request.payment.bankEnt,
            "bankOff" to request.payment.bankOff,
            "bankDC" to request.payment.bankDC,
            "bankAcc" to request.payment.bankAcc,

            // =====================================================
            // 10. CHECKBOXES DE TIPO DE CERTIFICADO
            // =====================================================
            "17 Antecedentes Penales" to if (request.certificateType == CertificateType.CRIMINAL_RECORDS) "On" else "Off",
            "18 Últimas voluntades" to if (request.certificateType == CertificateType.LAST_WILL) "On" else "Off",
            "19 Contrato de seguros de cobertura de fallecimiento" to if (request.certificateType == CertificateType.LIFE_INSURANCE) "On" else "Off",
        ).filterValues { it.isNotBlank() }
    }
}