package com.ceropapeleo.backend.dto

import kotlinx.serialization.Serializable

@Serializable
enum class CertificateType { CRIMINAL_RECORD, LAST_WILL, DEATH_INSURANCE_CONTRACTS }

@Serializable
enum class PaymentMethod { CASH, DIRECT_DEBIT }

@Serializable
data class GenerateRequest(
    val certificateType: CertificateType,
    val applicant: Applicant,
    val destination: Destination? = null,
    val payment: Payment,
    val signature: Signature,
    val deathRelatedDetails: DeathRelatedDetails? = null,
    val criminalRecordDetails: String? = null // TODO: Definir campos cuando el contrato se amplíe
)

@Serializable
data class Applicant(
    val documentId: String,
    val surname1: String,
    val surname2: String? = null,
    val name: String,
    val address: Address,
    val contact: Contact
)

@Serializable
data class Address(val street: String, val city: String, val province: String, val zipCode: String)

@Serializable
data class Contact(val email: String? = null, val mobilePhone: String? = null)

@Serializable
data class Destination(val entity: String? = null)

@Serializable
data class Payment(
    val amountEur: Double,
    val paymentMethod: PaymentMethod,
    val customerAccount: String? = null
)

@Serializable
data class Signature(val place: String, val date: String) // ISO YYYY-MM-DD

@Serializable
data class DeathRelatedDetails(
    val deceased: Deceased,
    val lastWillExtra: LastWillExtra? = null
)

@Serializable
data class Deceased(val name: String, val surname1: String, val dateOfDeath: String)

@Serializable
data class LastWillExtra(
    val willDate: String? = null,
    val notary: String? = null,
    val grantPlace: String? = null,
    val spousesFullName: String? = null
)