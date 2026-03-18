package com.ceropapeleo.backend

import com.ceropapeleo.backend.service.PdfService
import com.ceropapeleo.backend.dto.ErrorResponse
import com.ceropapeleo.backend.dto.FieldError
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.utils.io.jvm.javaio.*
import org.slf4j.LoggerFactory

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module).start(wait = true)
}

fun Application.module() {
    val logger = LoggerFactory.getLogger("Application")
    install(ContentNegotiation) { json() }
    install(CallLogging)

    val pdfService = PdfService()

    routing {
        post("/generate") {
            try {
                val userData = call.receive<Map<String, String>>()

                // 1. LÓGICA DE VALIDACIÓN (Usando tu ErrorResponse)
                val camposBase = listOf("documentId", "name", "surname1", "city", "postalCode")
                val esUltimasVoluntades = userData["certificateType"] == "LAST_WILL"

                val camposFallecido = if (esUltimasVoluntades) listOf("birthCity") else emptyList()
                val obligatorios = camposBase + camposFallecido

                val faltantes = obligatorios.filter { !userData.containsKey(it) || userData[it].isNullOrBlank() }

                if (faltantes.isNotEmpty()) {
                    // Mapeamos los faltantes a tu lista de FieldError
                    val detallesErrores = faltantes.map { campo ->
                        FieldError(field = campo, message = "El campo '$campo' es obligatorio")
                    }

                    call.respond(HttpStatusCode.BadRequest, ErrorResponse(
                        errorCode = "VALIDATION_ERROR",
                        message = "Faltan datos obligatorios para el Modelo 790",
                        errors = detallesErrores
                    ))
                    return@post
                }

                // 2. PROCESAMIENTO
                val templateFile = java.io.File("formulario-790-006_es_es.pdf")
                if (!templateFile.exists()) {
                    call.respond(HttpStatusCode.InternalServerError, ErrorResponse(
                        errorCode = "FILE_NOT_FOUND",
                        message = "El PDF base no se encuentra en el servidor"
                    ))
                    return@post
                }

                val pdfBytes = pdfService.fillPdf(templateFile.inputStream(), userData)

                call.response.header(HttpHeaders.ContentDisposition, "attachment; filename=\"Modelo790_Relleno.pdf\"")
                call.respondBytes(pdfBytes, ContentType.Application.Pdf, HttpStatusCode.Created)

            } catch (e: Exception) {
                logger.error("Error crítico: ${e.message}")
                call.respond(HttpStatusCode.InternalServerError, ErrorResponse(
                    errorCode = "SERVER_ERROR",
                    message = e.message ?: "Error interno desconocido"
                ))
            }
        }

        post("/fill-pdf") {
            val multipart = call.receiveMultipart()
            multipart.forEachPart { part ->
                if (part is PartData.FileItem) {
                    pdfService.inspectAndListFields(part.provider().toInputStream())
                }
                part.dispose()
            }
            call.respondText("Inspección finalizada.")
        }
    }
}