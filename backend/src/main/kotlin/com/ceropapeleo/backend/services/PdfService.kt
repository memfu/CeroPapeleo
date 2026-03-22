package com.ceropapeleo.backend.services

import org.apache.pdfbox.Loader
import org.slf4j.LoggerFactory
import java.io.ByteArrayOutputStream
import java.io.InputStream
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject
import java.util.Base64

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
     */fun fillPdfForm(
        pdfStream: InputStream,
        data: Map<String, String>,
        signatureImageBase64: String? = null
    ): ByteArray {
        val outputStream = ByteArrayOutputStream()

        try {
            Loader.loadPDF(pdfStream.readBytes()).use { document ->
                val acroForm = document.documentCatalog.acroForm

                if (acroForm != null) {
                    data.forEach { (fieldName, value) ->
                        val field = acroForm.getField(fieldName)
                        if (field != null) {
                            field.setValue(value)
                        } else {
                            logger.warn("Campo no encontrado: {}", fieldName)
                        }
                    }
                } else {
                    logger.warn("El PDF no contiene AcroForm")
                }

                if (!signatureImageBase64.isNullOrBlank()) {
                    try {
                        val cleanBase64 = signatureImageBase64.substringAfter("base64,", signatureImageBase64)
                        val imageBytes = Base64.getDecoder().decode(cleanBase64)

                        val pdImage = PDImageXObject.createFromByteArray(
                            document,
                            imageBytes,
                            "signature"
                        )

                        val page = document.getPage(0)

                        PDPageContentStream(
                            document,
                            page,
                            PDPageContentStream.AppendMode.APPEND,
                            true,
                            true
                        ).use { contentStream ->
                            contentStream.drawImage(
                                pdImage,
                                330f, // x
                                145f, // y
                                140f, // width
                                45f   // height
                            )
                        }

                        logger.info("Firma incrustada en el PDF correctamente")
                    } catch (e: Exception) {
                        logger.error("Error al incrustar la firma", e)
                    }
                }

                document.save(outputStream)
            }
        } catch (e: Exception) {
            logger.error("Error al rellenar PDF", e)
            throw e
        }

        return outputStream.toByteArray()
    }
}