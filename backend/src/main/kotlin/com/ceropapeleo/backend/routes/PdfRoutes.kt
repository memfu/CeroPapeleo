package com.ceropapeleo.backend.routes

import com.ceropapeleo.backend.logic.PdfMapper
import com.ceropapeleo.backend.services.PdfService
import com.ceropapeleo.backend.services.S3Service
import com.ceropapeleo.backend.services.DynamoService
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.utils.io.readRemaining
import kotlinx.coroutines.launch
import kotlinx.io.readByteArray
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory

fun Route.pdfRoutes(pdfService: PdfService) {
    val logger = LoggerFactory.getLogger("PdfRoutes")

    get("/") {
        call.respondText("🚀 Backend de CeroPapeleo funcionando en AWS correctamente", ContentType.Text.Plain)
    }
    get("/health") {
        call.respond(mapOf("status" to "OK", "message" to "Backend funcionando correctamente"))
    }

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
                        when (part.name) {
                            "userData", "user_data" -> userDataRaw = part.value
                            "signature" -> {
                                signatureImageBase64 = part.value
                                logger.info("🖊️ Firma recibida")
                            }
                        }
                    }
                    else -> part.dispose()
                }
                part.dispose()
            }

            if (pdfBytes == null || userDataRaw == null) {
                return@post call.respond(HttpStatusCode.BadRequest, "Falta el PDF o los datos del usuario")
            }

            val userData: Map<String, String> = Json.decodeFromString(userDataRaw)

            // FIX TC-02: Validación de documentId obligatorio
            val dniUsuario = userData["documentId"] ?: userData["dni"]
            if (dniUsuario.isNullOrBlank()) {
                return@post call.respond(
                    HttpStatusCode.BadRequest,
                    "El campo documentId es obligatorio"
                )
            }

            // FIX TC-03: Validación condicional para trámite de defunción
            val isLastWill = userData["18 Últimas voluntades"] == "On"
            val isLifeInsurance = userData["19 Contrato de seguros de cobertura de fallecimiento"] == "On"
            if (isLastWill || isLifeInsurance) {
                val deceasedName = userData["deceasedName"]
                val deceasedSurname1 = userData["deceasedSurname1"]
                if (deceasedName.isNullOrBlank() && deceasedSurname1.isNullOrBlank()) {
                    return@post call.respond(
                        HttpStatusCode.BadRequest,
                        "Para trámites de defunción es obligatorio el nombre o apellido del fallecido"
                    )
                }
            }

            val translatedData = PdfMapper.transformToPdfFields(userData)

            val pdfResult = pdfService.fillPdfForm(
                pdfBytes.inputStream(),
                translatedData,
                signatureImageBase64
            )

            logger.info("✅ PDF rellenado con éxito localmente")

            val nombreArchivo = "790_${dniUsuario}_${System.currentTimeMillis()}.pdf"

            val appScope = call.application
            appScope.launch {
                try {
                    logger.info("⏳ [Background] Iniciando proceso en la nube para DNI: $dniUsuario")

                    val urlPublica = S3Service.uploadPdf(nombreArchivo, pdfResult)

                    if (urlPublica != null) {
                        logger.info("☁️ [Background] S3 OK: $urlPublica")

                        DynamoService.saveTramite(
                            dni = dniUsuario,
                            s3Url = urlPublica,
                            tramiteTipo = "Modelo 790 - Tasa"
                        )
                        logger.info("🗄️ [Background] DynamoDB OK. Registro completado.")
                    }
                } catch (e: Exception) {
                    logger.error("⚠️ [Background] Error en proceso AWS: ${e.message}")
                }
            }

            call.response.header(HttpHeaders.ContentDisposition, "attachment; filename=\"$nombreArchivo\"")
            call.respondBytes(pdfResult, ContentType.Application.Pdf, HttpStatusCode.OK)

        } catch (e: Exception) {
            logger.error("💥 Error crítico en /fill-pdf", e)
            call.respond(HttpStatusCode.InternalServerError, "Error interno del servidor")
        }
    }
}