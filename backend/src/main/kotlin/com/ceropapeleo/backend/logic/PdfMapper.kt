package com.ceropapeleo.backend.logic

import org.slf4j.LoggerFactory

object PdfMapper {
    private val logger = LoggerFactory.getLogger(PdfMapper::class.java)

    /**
     * Mapeo Técnico Completo: Nombre del campo en PDF -> Clave en JSON
     */
    private val MODELO_790_MAP = mapOf(
        // 1. Datos del solicitante
        "nie" to "documentId",
        "2 PRIMER APELLIDO DEL SOLICITANTE" to "surname1",
        "3 SEGUNDO APELLIDO" to "surname2",
        "4 NOMBRE" to "name",

        // 2. Dirección
        "5 DOMICILIO CALLEPLAZAAVENIDA" to "street",
        "6 NÚMERO" to "number",
        "ESCALERA" to "staircase",
        "8 PISO" to "floor",
        "9 PUERTA" to "door",
        "11 DOMICILIO MUNICIPIO" to "city",
        "12 DOMICILIO PROVINCIA" to "province",
        "12 DOMICILIO PAIS" to "country",
        "14 CÓDIGO POSTAL" to "postalCode",

        // 3. Contacto
        "10 TELEFONOS FIJO YO MÓVIL" to "mobilePhone",
        "15 CORREO ELECTRÓNICO" to "email",

        // 4. Destino
        "20 PAÍS DE DESTINO" to "destinationCountry",
        "21 AUTORIDAD O ENTIDAD ANTE LA QUE DEBE SURTIR EFECTOS" to "authorityOrEntity",

        // 5. Datos del Fallecido
        "33 NIFNIE" to "deceasedDocumentId",
        "34 PRIMER APELLIDO DE LA PERSONA FALLECIDA" to "deceasedSurname1",
        "35 SEGUNDO APELLIDO" to "deceasedSurname2",
        "36 NOMBRE" to "deceasedName",
        "37 FECHA DE DEFUNCIÓN" to "deathDate",
        "38 POBLACIÓN DE DEFUNCIÓN" to "deathCity",
        "39 FECHA DE NACIMIENTO" to "birthDate",
        "39 POBLACION  DE NACIMIENTO" to "birthCity", 

        // 6. Testamento y Firma
        "FECHA DEL TESTAMENTO" to "willDate",
        "NOTARIO" to "notary",
        "LUGAR DE OTORGAMIENTO" to "grantPlace",
        "CONYUGE" to "spousesFullName",
        "FECHA LUGAR" to "signaturePlace",
        "FECHA" to "signatureDate",

        // 7. Tipo certificado y Pago
        "18 Últimas voluntades" to "certificateType",
        "EUROS" to "amountEur"
    )

    fun transformToPdfFields(userData: Map<String, String>): Map<String, String> {
        val finalMap = mutableMapOf<String, String>()

        MODELO_790_MAP.forEach { (pdfId, jsonKey) ->
            val value = userData[jsonKey]

            if (!value.isNullOrBlank()) {
                // REGLA DE ORO DE MARILÚ: Usamos "On" para que la X aparezca
                if (jsonKey == "certificateType" && value == "LAST_WILL") {
                    finalMap[pdfId] = "On" 
                    logger.info("✅ Checkbox activado con 'On': $pdfId")
                } else {
                    finalMap[pdfId] = value
                    logger.info("⚡ Mapping: App($jsonKey) -> PDF($pdfId) = $value")
                }
            }
        }
        return finalMap
    }
}