package com.ceropapeleo.backend.services

import org.apache.pdfbox.Loader
import org.apache.pdfbox.pdmodel.PDDocument
import org.slf4j.LoggerFactory
import java.io.ByteArrayOutputStream
import java.io.InputStream

class PdfService {
    private val logger = LoggerFactory.getLogger(PdfService::class.java)

    /**
     * FUNCIÓN 1: Inspecciona los campos (La que ya tenías)
     */
    fun inspectPdfFields(pdfStream: InputStream): List<String> {
        val pdfBytes = pdfStream.readBytes()
        val document: PDDocument = Loader.loadPDF(pdfBytes)
        try {
            val acroForm = document.documentCatalog.acroForm
            if (acroForm == null) return emptyList()
            return acroForm.fields.map { it.fullyQualifiedName }
        } finally {
            document.close()
        }
    }

    /**
     * FUNCIÓN 2: Rellena los campos (LA QUE TE FALTA)
     * @param pdfStream El PDF original
     * @param data El mapa de datos YA TRADUCIDO por el PdfMapper
     */
    fun fillPdfForm(pdfStream: InputStream, data: Map<String, String>): ByteArray {
        val pdfBytes = pdfStream.readBytes()
        val document: PDDocument = Loader.loadPDF(pdfBytes)
        val outputStream = ByteArrayOutputStream()

        try {
            val acroForm = document.documentCatalog.acroForm

            if (acroForm != null) {
                // Recorremos los datos traducidos y los inyectamos en el PDF
                data.forEach { (fieldName, value) ->
                    val field = acroForm.getField(fieldName)
                    if (field != null) {
                        field.setValue(value)
                        logger.info("Rellenado: Campo '$fieldName' con valor '$value'")
                    } else {
                        logger.warn("Aviso: El campo '$fieldName' no existe en este PDF oficial.")
                    }
                }

                // Opcional: Esto hace que el PDF no sea editable después de rellenarlo
                // acroForm.flatten()
            }

            document.save(outputStream)
            return outputStream.toByteArray()

        } catch (e: Exception) {
            logger.error("Error al rellenar el formulario PDF: ${e.message}")
            throw e
        } finally {
            document.close()
        }
    }
}