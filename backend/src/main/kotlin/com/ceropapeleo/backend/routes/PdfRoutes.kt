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

    post("/fill-pdf") {
        try {
            val multipart = call.receiveMultipart()
            var pdfBytes: ByteArray? = null
            var userDataRaw: String? = null
            var signatureImageBase64: String? = null

            multipart.forEachPart { part ->
                when (part) {
                    is PartData.FileItem -> {
                        if (part.name == "pdf_file") {
                            pdfBytes = part.provider().readRemaining().readByteArray()

                            logger.info("📄 PDF recibido (Tamaño: ${pdfBytes.size} bytes)")
                        }
                    }
                    is PartData.FormItem -> {
                        if (part.name == "userData" || part.name == "user_data") {
                            userDataRaw = part.value
                        }
                        if (part.name == "signature") {
                            signatureImageBase64 = part.value
                            logger.info("🖊️ Firma recibida")
                        }
                    }
                    else -> part.dispose()
                }
                part.dispose()
            }

            if (pdfBytes == null || userDataRaw == null) {
                return@post call.respond(HttpStatusCode.BadRequest, "Falta el PDF o datos")
            }

            val userData: Map<String, String> = Json.decodeFromString(userDataRaw)
            val translatedData = PdfMapper.transformToPdfFields(userData)

            val pdfResult = pdfService.fillPdfForm(
                pdfBytes.inputStream(),
                translatedData,
                signatureImageBase64
            )

            logger.info("✅ PDF rellenado con éxito")

            val s3Service = com.ceropapeleo.backend.services.S3Service()
            val dynamoService = com.ceropapeleo.backend.services.DynamoService()

            val dniUsuario = userData["documentId"] ?: userData["dni"] ?: "anonimo"
            val nombreArchivo = "790_${dniUsuario}_${System.currentTimeMillis()}.pdf"

            try {
                val urlPublica = s3Service.uploadPdf(nombreArchivo, pdfResult)
                if (urlPublica != null) {
                    logger.info("☁️ [S3] PDF subido: $urlPublica")
                    call.response.header("X-Cloud-URL", urlPublica)

                    dynamoService.saveTramite(
                        dni = dniUsuario,
                        s3Url = urlPublica,
                        tramiteTipo = "Modelo 790 - Tasa"
                    )
                }
            } catch (e: Exception) {
                logger.error("⚠️ [AWS] Fallo en la nube (pero seguimos): ${e.message}")
            }

            call.response.header(HttpHeaders.ContentDisposition, "attachment; filename=\"$nombreArchivo\"")
            call.respondBytes(pdfResult, ContentType.Application.Pdf, HttpStatusCode.OK)

        } catch (e: Exception) {
            logger.error("💥 Error crítico en /fill-pdf", e)
            call.respond(HttpStatusCode.InternalServerError, "Error interno del servidor")
        }
    }
}