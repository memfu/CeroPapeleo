package com.ceropapeleo.backend.service

import com.ceropapeleo.backend.logic.PdfMapper
import org.apache.pdfbox.Loader
import org.slf4j.LoggerFactory
import java.io.ByteArrayOutputStream
import java.io.InputStream

class PdfService {
    private val logger = LoggerFactory.getLogger(PdfService::class.java)

    fun fillPdf(templateStream: InputStream, userData: Map<String, String>): ByteArray {
        val bytes = templateStream.readAllBytes()
        val outputStream = ByteArrayOutputStream()

        Loader.loadPDF(bytes).use { document ->
            val acroForm = document.documentCatalog.acroForm
                ?: throw IllegalStateException("El PDF no tiene campos rellenables")

            val pdfFields = PdfMapper.transformToPdfFields(userData)

            pdfFields.forEach { (fieldId, value) ->
                val field = acroForm.getField(fieldId)
                if (field != null) {
                    try {
                        field.setValue(value)
                    } catch (e: Exception) {
                        logger.warn("Error campo $fieldId: ${e.message}")
                    }
                }
            }
            document.save(outputStream)
        }
        return outputStream.toByteArray()
    }

    fun inspectAndListFields(templateStream: InputStream) {
        val bytes = templateStream.readAllBytes()
        Loader.loadPDF(bytes).use { document ->
            document.documentCatalog.acroForm?.fields?.forEach { field ->
                logger.info("ID: [${field.fullyQualifiedName}]")
            }
        }
    }
}