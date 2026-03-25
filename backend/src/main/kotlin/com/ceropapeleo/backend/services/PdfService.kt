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

                        // AJUSTAR estas coordenadas tras probar
                        val signatureX = 90f
                        val signatureY = 95f
                        val signatureMaxWidth = 140f
                        val signatureMaxHeight = 28f

                        val imageWidth = pdImage.width.toFloat()
                        val imageHeight = pdImage.height.toFloat()
                        val imageAspectRatio = imageWidth / imageHeight

                        val boxAspectRatio = signatureMaxWidth / signatureMaxHeight

                        val finalSignatureWidth: Float
                        val finalSignatureHeight: Float

                        if (imageAspectRatio > boxAspectRatio) {
                            finalSignatureWidth = signatureMaxWidth
                            finalSignatureHeight = signatureMaxWidth / imageAspectRatio
                        } else {
                            finalSignatureHeight = signatureMaxHeight
                            finalSignatureWidth = signatureMaxHeight * imageAspectRatio
                        }

                        val finalSignatureX = signatureX + (signatureMaxWidth - finalSignatureWidth) / 2f
                        val finalSignatureY = signatureY + (signatureMaxHeight - finalSignatureHeight) / 2f

                        val pagesToSign = minOf(3, document.numberOfPages)

                        for (pageIndex in 0 until pagesToSign) {
                            val page = document.getPage(pageIndex)

                            PDPageContentStream(
                                document,
                                page,
                                PDPageContentStream.AppendMode.APPEND,
                                true,
                                true
                            ).use { contentStream ->
                                contentStream.drawImage(
                                    pdImage,
                                    finalSignatureX,
                                    finalSignatureY,
                                    finalSignatureWidth,
                                    finalSignatureHeight
                                )
                            }
                        }

                        logger.info("✅ Firma incrustada en las primeras $pagesToSign páginas del PDF")
                    } catch (e: Exception) {
                        logger.error("❌ Error al incrustar la firma", e)
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