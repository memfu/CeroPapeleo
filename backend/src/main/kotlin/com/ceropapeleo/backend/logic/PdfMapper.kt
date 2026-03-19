package com.ceropapeleo.backend.logic

import org.slf4j.LoggerFactory

object PdfMapper {
    private val logger = LoggerFactory.getLogger(PdfMapper::class.java)

    private val MODELO_790_MAP = mapOf(
        // 1. Datos del solicitante (Usando tus nombres verificados)
        "nie" to "documentId", // 👈 Tu corrección del inspector
        "2 PRIMER APELLIDO DEL SOLICITANTE" to "surname1",
        "3 SEGUNDO APELLIDO" to "surname2",
        "4 NOMBRE" to "name",

        // 2. Dirección y Vivienda (Mezcla de ambos)
        "5 DOMICILIO CALLEPLAZAAVENIDA" to "street",
        "6 NÚMERO" to "number",
        "ESCALERA" to "staircase",
        "8 PISO" to "floor",
        "9 PUERTA" to "door",
        "11 DOMICILIO MUNICIPIO" to "city",
        "12 DOMICILIO PROVINCIA" to "province",
        "14 CÓDIGO POSTAL" to "postalCode",

        // 3. Contacto
        "10 TELEFONOS FIJO YO MÓVIL" to "mobilePhone",
        "15 CORREO ELECTRÓNICO" to "email",

        // 5. Datos del Fallecido (Tus logs confirman que estos son los IDs)
        "33 NIFNIE" to "deceasedDocumentId",
        "34 PRIMER APELLIDO DE LA PERSONA FALLECIDA" to "deceasedSurname1",
        "35 SEGUNDO APELLIDO" to "deceasedSurname2",
        "36 NOMBRE" to "deceasedName",
        "37 FECHA DE DEFUNCIÓN" to "deathDate",
        "38 POBLACIÓN DE DEFUNCIÓN" to "deathCity",
        "39 POBLACION  DE NACIMIENTO" to "birthCity", // 👈 Con doble espacio como en tus logs

        // 6. Testamento y Otros (Nuevos de María)
        "FECHA DEL TESTAMENTO" to "willDate",
        "NOTARIO" to "notary",
        "LUGAR DE OTORGAMIENTO" to "grantPlace",
        "CONYUGE" to "spousesFullName",

        // 8. Tipo certificado (Tu Regla de Oro)
        "18 Últimas voluntades" to "certificateType"
    )

    fun transformToPdfFields(userData: Map<String, String>): Map<String, String> {
        val finalMap = mutableMapOf<String, String>()

        MODELO_790_MAP.forEach { (pdfId, jsonKey) ->
            val value = userData[jsonKey]

            if (!value.isNullOrBlank()) {
                if (jsonKey == "certificateType" && value == "LAST_WILL") {
                    finalMap[pdfId] = "On" // REGLA ORO: Activamos la X con "On"
                    logger.info("✅ Checkbox activado: $pdfId")
                } else {
                    finalMap[pdfId] = value
                    logger.info("⚡ Mapping: App($jsonKey) -> PDF($pdfId) = $value")
                }
            }
        }
        return finalMap
    }
}