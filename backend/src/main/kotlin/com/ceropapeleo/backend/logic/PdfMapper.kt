package com.ceropapeleo.backend.logic

import org.slf4j.LoggerFactory

object PdfMapper {
    private val logger = LoggerFactory.getLogger(PdfMapper::class.java)

    private val MODELO_790_MAP = mapOf(
        // 1. Datos del solicitante
        "nie" to "documentId",
        "2 PRIMER APELLIDO DEL SOLICITANTE" to "surname1",
        "3 SEGUNDO APELLIDO" to "surname2",
        "4 NOMBRE" to "name",

        // 2. Dirección y Contacto
        "5 DOMICILIO CALLEPLAZAAVENIDA" to "street",
        "6 NÚMERO" to "number",
        "11 DOMICILIO MUNICIPIO" to "city",
        "12 DOMICILIO PROVINCIA" to "province",
        "14 CÓDIGO POSTAL" to "postalCode",
        "10 TELEFONOS FIJO YO MÓVIL" to "mobilePhone",
        "15 CORREO ELECTRÓNICO" to "email",

        // 5. Datos del Fallecido (La sección de Juan Pérez)
        "33 NIFNIE" to "deceasedDocumentId",
        "34 PRIMER APELLIDO DE LA PERSONA FALLECIDA" to "deceasedSurname1",
        "35 SEGUNDO APELLIDO" to "deceasedSurname2",
        "36 NOMBRE" to "deceasedName",
        "37 FECHA DE DEFUNCIÓN" to "deathDate",
        "38 POBLACIÓN DE DEFUNCIÓN" to "deathCity",
        "39 POBLACION  DE NACIMIENTO" to "birthCity",

        // 8. Tipo certificado (Regla de Oro de Marilú)
        "18 Últimas voluntades" to "certificateType"
    )

    fun transformToPdfFields(userData: Map<String, String>): Map<String, String> {
        val finalMap = mutableMapOf<String, String>()

        MODELO_790_MAP.forEach { (pdfId, jsonKey) ->
            val value = userData[jsonKey]

            if (!value.isNullOrBlank()) {
                if (jsonKey == "certificateType" && value == "LAST_WILL") {
                    finalMap[pdfId] = "On" // REGLA ORO: Activamos la X
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