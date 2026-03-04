
package com.ceropapeleo.ui.forms
enum class CertificateType { CRIMINAL_RECORD, LAST_WILL, DEATH_INSURANCE_CONTRACTS }
enum class PaymentMethod { CASH, DIRECT_DEBIT }

data class GenerateRequest(
    val certificateType: CertificateType = CertificateType.LAST_WILL,
    val applicant: Person = Person(),
    val address: Address = Address(),
    val contact: Contact = Contact(),
    val destination: Destination = Destination(),
    val payment: Payment = Payment(),
    val signature: Signature = Signature(),
    val deathRelatedDetails: DeathRelatedDetails? = null,
    val lastWillExtra: LastWillExtra? = null
)

data class Person(
    val documentId: String = "",
    val firstSurname: String = "",
    val secondSurname: String = "",
    val name: String = ""
)

data class Address(
    val street: String = "",
    val number: String = "",
    val postalCode: String = "",
    val city: String = "",
    val province: String = "",
    val country: String = "España"
)

data class Contact(val mobilePhone: String = "", val email: String = "")

data class Destination(val country: String = "España", val authorityOrEntity: String = "")

data class Payment(
    val amountEur: String = "3.82",
    val paymentMethod: PaymentMethod = PaymentMethod.CASH
)

data class Signature(
    val place: String = "",
    val date: String = "", // YYYY-MM-DD
    val postalDeliveryAuthorized: Boolean = false
)

data class DeathRelatedDetails(
    val deceased: Person = Person(),
    val birthDate: String = "",
    val birthCity: String = "",
    val deathDate: String = "",
    val deathCity: String = ""
)

data class LastWillExtra(
    val disappearanceDate: String = "",
    val notary: String = "",
    val grantPlace: String = "",
    val spousesFullName: String = ""
)