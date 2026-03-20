package com.ceropapeleo.backend.routes

import com.ceropapeleo.backend.logic.PdfMapper
import com.ceropapeleo.backend.services.PdfService
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.utils.io.readRemaining
import kotlinx.io.readByteArray
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory

fun Route.pdfRoutes(pdfService: PdfService) {
    val logger = LoggerFactory.getLogger("PdfRoutes")

    /**
     * Endpoint: /fill-pdf
     * Recibe el PDF del WebViewer + los datos del usuario vía Multipart
     */
    post("/fill-pdf") {
        try {
            val multipart = call.receiveMultipart()
            var pdfBytes: ByteArray? = null
            var userDataRaw: String? = null

            // Abrimos el paquete Multipart
            multipart.forEachPart { part ->
                when (part) {
                    is PartData.FileItem -> {
                        // Captura el archivo PDF físico enviado por Cris
                        pdfBytes = part.provider().readRemaining().readByteArray()
                        logger.info("📄 PDF recibido (Tamaño: ${pdfBytes?.size} bytes)")
                    }
                    is PartData.FormItem -> {
                        // Captura el JSON de datos. Soporta ambos nombres por si acaso.
                        if (part.name == "userData" || part.name == "user_data") {
                            userDataRaw = part.value
                        }
                    }
                    else -> part.dispose()
                }
            }

            // Validaciones de seguridad
            if (pdfBytes == null || userDataRaw == null) {
                logger.error("❌ Petición incompleta: falta PDF o datos")
                return@post call.respond(HttpStatusCode.BadRequest, "Falta el PDF o el campo userData")
            }

            // Procesamiento
            val userData: Map<String, String> = Json.decodeFromString(userDataRaw!!)
            val translatedData = PdfMapper.transformToPdfFields(userData)

            // Rellenamos el PDF físico que nos ha llegado desde el móvil
            val pdfResult = pdfService.fillPdfForm(pdfBytes!!.inputStream(), translatedData)

            logger.info("✅ PDF con número único rellenado con éxito")

            // Respuesta binaria para que el móvil lo guarde
            call.response.header(HttpHeaders.ContentDisposition, "attachment; filename=\"Modelo790_Final.pdf\"")
            call.respondBytes(pdfResult, ContentType.Application.Pdf, HttpStatusCode.OK)

        } catch (e: Exception) {
            logger.error("💥 Error crítico en /fill-pdf: ${e.message}")
            call.respond(HttpStatusCode.InternalServerError, "Error interno del servidor")
        }
    }
}