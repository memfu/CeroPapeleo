package com.ceropapeleo.backend.logic

import org.slf4j.LoggerFactory

/**
 * El Mapper actúa como un "Traductor".
 * Sabe que el dato "name" de Android debe ir al campo "f1_01(0)" del PDF.
 */
object PdfMapper {
    private val logger = LoggerFactory.getLogger(PdfMapper::class.java)

    // Diccionario de equivalencias: CampoPDF -> ClaveJSON
    // Si el Ministerio cambia el PDF, solo tocamos esta lista.
    private val MODELO_790_MAP = mapOf(
        "f1_01(0)" to "name",
        "f1_02(0)" to "surname1",
        "f1_03(0)" to "surname2",
        "dni_nie"  to "documentId",
        "prov_res" to "province"
    )

    /**
     * Traduce el JSON que envía la App al formato que entiende el PDF.
     * @param userData Los datos originales de la App (ej: {"name": "Marilú"})
     * @return Los datos listos para el PDF (ej: {"f1_01(0)": "Marilú"})
     */
    fun transformToPdfFields(userData: Map<String, String>): Map<String, String> {
        val finalMap = mutableMapOf<String, String>()

        MODELO_790_MAP.forEach { (pdfField, jsonKey) ->
            val value = userData[jsonKey]
            if (!value.isNullOrBlank()) {
                finalMap[pdfField] = value
                logger.info("Mapping: JSON '$jsonKey' -> PDF '$pdfField' (Valor: $value)")
            }
        }

        return finalMap
    }
}