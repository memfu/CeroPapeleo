
package com.ceropapeleo.ui.forms

enum class CertificateType {
    CRIMINAL_RECORD,
    LAST_WILL,
    DEATH_INSURANCE_CONTRACTS
}

enum class PaymentMethod {
    CASH,
    DIRECT_DEBIT
}

data class GenerateRequest(
    val certificateType: CertificateType = CertificateType.LAST_WILL,
    val applicant: Applicant = Applicant(),
    val destination: Destination = Destination(),
    val payment: Payment = Payment(),
    val signature: Signature = Signature(),
    val deathRelatedDetails: DeathRelatedDetails? = null
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

data class Contact(
    val mobilePhone: String = "",
    val email: String = ""
)

data class Destination(
    val country: String = "España",
    val authorityOrEntity: String = ""
)

data class Payment(
    val amountEur: Double = 3.82,
    val paymentMethod: PaymentMethod = PaymentMethod.CASH,
    val customerAccount: String? = null
)

data class Signature(
    val place: String = "",
    val date: String = "", // YYYY-MM-DD
    val postalDeliveryAuthorized: Boolean = false
)

data class DeathRelatedDetails(
    val deceased: Deceased = Deceased(),
    val lastWillExtra: LastWillExtra? = null
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