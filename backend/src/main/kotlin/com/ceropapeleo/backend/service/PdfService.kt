package com.ceropapeleo.backend.service

import org.apache.pdfbox.Loader
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm
import org.slf4j.LoggerFactory
import java.io.InputStream

class PdfService {
    private val logger = LoggerFactory.getLogger(PdfService::class.java)

    /**
     * Inspecciona el PDF buscando el formulario AcroForm y lista sus campos.
     */
    fun inspectAndListFields(templateStream: InputStream) {
        try {
            val bytes = templateStream.readAllBytes()

            Loader.loadPDF(bytes).use { document ->
                val acroForm: PDAcroForm? = document.documentCatalog.acroForm

                // Si el PDF no tiene campos, lanzamos un error que Application capture
                if (acroForm == null) {
                    logger.error("AUDITORÍA: El PDF no contiene campos de formulario (AcroForm es null).")
                    throw IllegalArgumentException("El archivo PDF no contiene campos rellenables.")
                }

                logger.info("--- INICIO DE INSPECCIÓN TÉCNICA ---")
                acroForm.fields.forEach { field ->
                    // Este es el nombre exacto que necesitamos para el mapeo
                    logger.info("ID Detectado: [${field.fullyQualifiedName}] | Tipo: ${field.fieldType}")
                }
                logger.info("--- FIN DE INSPECCIÓN TÉCNICA ---")
            }
        } catch (e: Exception) {
            logger.error("Error crítico en la apertura del PDF: ${e.message}")
            throw e
        }
    }
}