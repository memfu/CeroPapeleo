package com.unirfp.ceropapeleo.model

/**
 * Representa la solicitud completa para generar el formulario oficial.
 * Este modelo agrupa todos los datos recolectados en la UI que el backend
 * inyectará en los campos del PDF del Modelo 790.
 */
data class GenerateRequest(
    val certificateType: String = "LAST_WILL",
    val applicant: Applicant = Applicant(),
    val destination: Destination = Destination(),
    val payment: Payment = Payment(),
    val signature: Signature = Signature(),
    val deathRelatedDetails: DeathRelatedDetails = DeathRelatedDetails()
)

/**
 * Datos de identidad y localización de la persona que solicita el trámite.
 */
data class Applicant(
    val documentId: String = "",
    val firstSurname: String = "",
    val secondSurname: String = "",
    val name: String = "",
    val address: Address = Address(),
    val contact: Contact = Contact()
)

/**
 * Información detallada del domicilio para notificaciones legales.
 */
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

/**
 * Datos de contacto para comunicaciones rápidas.
 */
data class Contact(
    val mobilePhone: String = "",
    val email: String = ""
)

/**
 * Entidad u organismo al que se dirige el certificado generado.
 */
data class Destination(
    val country: String = "España",
    val authorityOrEntity: String = ""
)

/**
 * Detalles de la tasa administrativa.
 * El monto por defecto (3.78) corresponde a la tasa legal vigente para este trámite.
 */
data class Payment(
    val amountEur: Double = 3.78,
    val paymentMethod: String = "CASH"
)

/**
 * Metadatos de la firma y entrega del documento.
 */
data class Signature(
    val place: String = "",
    val date: String = "", // Formato recomendado: YYYY-MM-DD
    val postalDeliveryAuthorized: Boolean = true
)

/**
* Contenedor de información relacionada con certificados de fallecimiento.
 * En el MVP, lastWillExtra solo se usa cuando certificateType == "LAST_WILL".
 */
data class DeathRelatedDetails(
    val deceased: Deceased = Deceased(),
    val lastWillExtra: LastWillExtra = LastWillExtra()
)

/**
 * Datos de filiación e identificación del fallecido (Causante).
 */
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

/**
 * Información notarial necesaria para localizar el último testamento.
 */
data class LastWillExtra(
    val willDate: String = "",
    val notary: String = "",
    val grantPlace: String = "",
    val spousesFullName: String = ""
)