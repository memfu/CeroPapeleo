package com.unirfp.ceropapeleo.model

/**
 * Modelos de datos que reflejan el contrato de la API
 * para mantener el estado del formulario.
 */
data class GenerateRequest(
    val certificateType: String = "LAST_WILL",
    val applicant: Applicant = Applicant(),
    val destination: Destination = Destination(),
    val payment: Payment = Payment(),
    val signature: Signature = Signature(),
    val deathRelatedDetails: DeathRelatedDetails = DeathRelatedDetails()
)

data class Applicant(
    val documentId: String = "",
    val firstSurname: String = "",
    val secondSurname: String = "",
    val name: String = "",
    val address: Address = Address(),
    val contact: Contact = Contact()
)

data class Address(
    val street: String = "",
    val number: String = "",
    val staircase: String = "",
    val floor: String = "",
    val door: String = "",
    val postalCode: String = "",
    val city: String = "",
    val province: String = "",
    val country: String = "España"
)

data class Contact(val mobilePhone: String = "", val email: String = "")

data class Destination(val country: String = "España", val authorityOrEntity: String = "")

data class Payment(val amountEur: Double = 3.78, val paymentMethod: String = "CASH")

data class Signature(
    val place: String = "",
    val date: String = "",
    val postalDeliveryAuthorized: Boolean = true
)

data class DeathRelatedDetails(
    val deceased: Deceased = Deceased(),
    val lastWillExtra: LastWillExtra = LastWillExtra()
)

data class Deceased(
    val documentId: String = "",
    val firstSurname: String = "",
    val secondSurname: String = "",
    val name: String = "",
    val birthDate: String = "",
    val birthCity: String = "",
    val deathDate: String = "",
    val deathCity: String = ""
)

data class LastWillExtra(
    val willDate: String = "",
    val notary: String = "",
    val grantPlace: String = "",
    val spousesFullName: String = ""
)