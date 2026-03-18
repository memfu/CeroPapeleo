package com.ceropapeleo.backend.logic

import org.slf4j.LoggerFactory

object PdfMapper {
    private val logger = LoggerFactory.getLogger(PdfMapper::class.java)

    private val MODELO_790_MAP = mapOf(
        "nie" to "documentId",
        "2 PRIMER APELLIDO DEL SOLICITANTE" to "surname1",
        "3 SEGUNDO APELLIDO" to "surname2",
        "4 NOMBRE" to "name",
        "5 DOMICILIO CALLEPLAZAAVENIDA" to "street",
        "6 NÚMERO" to "number",
        "11 DOMICILIO MUNICIPIO" to "city",
        "12 DOMICILIO PROVINCIA" to "province",
        "14 CÓDIGO POSTAL" to "postalCode",
        "10 TELEFONOS FIJO YO MÓVIL" to "mobilePhone",
        "15 CORREO ELECTRÓNICO" to "email",
        "39 POBLACION  DE NACIMIENTO" to "birthCity",
        "18 Últimas voluntades" to "certificateType"
    )

    fun transformToPdfFields(userData: Map<String, String>): Map<String, String> {
        val finalMap = mutableMapOf<String, String>()

        MODELO_790_MAP.forEach { (pdfId, jsonKey) ->
            val value = userData[jsonKey]

            if (!value.isNullOrBlank()) {
                if (jsonKey == "certificateType" && value == "LAST_WILL") {
                    finalMap[pdfId] = "On" // REGLA ORO: On para marcar la X
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