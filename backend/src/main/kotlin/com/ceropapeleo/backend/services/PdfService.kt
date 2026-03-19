package com.ceropapeleo.backend.services

import org.apache.pdfbox.Loader
import org.slf4j.LoggerFactory
import java.io.ByteArrayOutputStream
import java.io.InputStream

class PdfService {
    private val logger = LoggerFactory.getLogger(PdfService::class.java)

    /**
     * TAREA 1: Inspecciona los campos de cualquier PDF.
     */
    fun inspectPdfFields(pdfStream: InputStream): List<String> {
        return try {
            Loader.loadPDF(pdfStream.readBytes()).use { document ->
                val acroForm = document.documentCatalog.acroForm
                acroForm?.fields?.map { it.fullyQualifiedName } ?: emptyList()
            }
        } catch (e: Exception) {
            logger.error("Error al inspeccionar PDF: ${e.message}")
            emptyList()
        }
    }

    /**
     * TAREA 2 y 3: Rellena el PDF con los datos mapeados.
     */
    fun fillPdfForm(pdfStream: InputStream, data: Map<String, String>): ByteArray {
        val outputStream = ByteArrayOutputStream()
        try {
            Loader.loadPDF(pdfStream.readBytes()).use { document ->
                val acroForm = document.documentCatalog.acroForm
                if (acroForm != null) {
                    data.forEach { (fieldName, value) ->
                        val field = acroForm.getField(fieldName)
                        field?.setValue(value) ?: logger.warn("Campo no encontrado: $fieldName")
                    }
                }
                document.save(outputStream)
            }
        } catch (e: Exception) {
            logger.error("Error al rellenar PDF: ${e.message}")
            throw e
        }
        return outputStream.toByteArray()
    }
}