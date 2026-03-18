package com.ceropapeleo.backend

import com.ceropapeleo.backend.service.PdfService
import com.ceropapeleo.backend.dto.ErrorResponse
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.utils.io.jvm.javaio.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.calllogging.*

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module).start(wait = true)
}

fun Application.module() {
    install(ContentNegotiation) { json() }
    install(CallLogging)
    val pdfService = PdfService()

    routing {
        post("/fill-pdf") {
            try {
                val multipart = call.receiveMultipart()
                var fileWasProcessed = false

                multipart.forEachPart { part ->
                    if (part is PartData.FileItem) {
                        // Solución al error 500: Usamos el provider moderno para obtener el stream
                        val fileStream = part.provider().toInputStream()
                        pdfService.inspectAndListFields(fileStream)
                        fileWasProcessed = true
                    }
                    part.dispose()
                }

                if (fileWasProcessed) {
                    call.respond(HttpStatusCode.OK, "✅ Inspección finalizada. Revisa la terminal de IntelliJ.")
                } else {
                    // Usamos tu ErrorResponse
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse(
                        errorCode = "MISSING_FILE",
                        message = "No se ha encontrado ningún archivo en la petición"
                    ))
                }

            } catch (e: IllegalArgumentException) {
                // Captura el error de PDF sin campos
                call.respond(HttpStatusCode.UnprocessableEntity, ErrorResponse(
                    errorCode = "INVALID_FORM",
                    message = e.message ?: "El PDF no es válido"
                ))
            } catch (e: Exception) {
                // Captura errores técnicos generales
                call.respond(HttpStatusCode.InternalServerError, ErrorResponse(
                    errorCode = "SERVER_ERROR",
                    message = "Error interno al procesar el PDF",
                    errors = null // Aquí podrías añadir más detalles si quisieras
                ))
            }
        }
    }
}