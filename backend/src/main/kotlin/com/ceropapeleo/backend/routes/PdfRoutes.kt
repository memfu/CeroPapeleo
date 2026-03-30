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

    // 1. RUTA DE PRUEBA: Para ver en el navegador y evitar el 404
    get("/") {
        call.respondText("🚀 Backend de CeroPapeleo funcionando en AWS correctamente", ContentType.Text.Plain)
    }

    // 2. RUTA PRINCIPAL: Rellenado de PDF y subida a AWS
    post("/fill-pdf") {
        try {
            val multipart = call.receiveMultipart()
            var pdfBytes: ByteArray? = null
            var userDataRaw: String? = null
            var signatureImageBase64: String? = null

            // Procesamiento de los datos recibidos (Multipart)
            multipart.forEachPart { part ->
                when (part) {
                    is PartData.FileItem -> {
                        if (part.name == "pdf_file") {
                            pdfBytes = part.provider().readRemaining().readByteArray()
                            logger.info("📄 PDF recibido (Tamaño: ${pdfBytes?.size} bytes)")
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

            // Lógica de rellenado del PDF
            val userData: Map<String, String> = Json.decodeFromString(userDataRaw!!)
            val translatedData = PdfMapper.transformToPdfFields(userData)

            val pdfResult = pdfService.fillPdfForm(
                pdfBytes!!.inputStream(),
                translatedData,
                signatureImageBase64
            )

            logger.info("✅ PDF rellenado con éxito localmente")

            // Preparar metadatos para AWS
            val dniUsuario = userData["documentId"] ?: userData["dni"] ?: "anonimo"
            val nombreArchivo = "790_${dniUsuario}_${System.currentTimeMillis()}.pdf"

            // Tareas de AWS en SEGUNDO PLANO (S3 y DynamoDB)
            val appScope = call.application
            appScope.launch {
                try {
                    logger.info("⏳ [Background] Iniciando proceso en la nube para DNI: $dniUsuario")

                    // 1. Subida a S3
                    val urlPublica = S3Service.uploadPdf(nombreArchivo, pdfResult)

                    if (urlPublica != null) {
                        logger.info("☁️ [Background] S3 OK: $urlPublica")

                        // 2. Guardado en DynamoDB
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

            // RESPUESTA INMEDIATA AL MÓVIL (Envío del archivo rellenado)
            call.response.header(HttpHeaders.ContentDisposition, "attachment; filename=\"$nombreArchivo\"")
            call.respondBytes(pdfResult, ContentType.Application.Pdf, HttpStatusCode.OK)

        } catch (e: Exception) {
            logger.error("💥 Error crítico en /fill-pdf", e)
            call.respond(HttpStatusCode.InternalServerError, "Error interno del servidor")
        }
    }
}