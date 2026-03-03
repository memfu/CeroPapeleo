package com.ceropapeleo.backend

import com.ceropapeleo.backend.dto.ErrorResponse
import com.ceropapeleo.backend.dto.GenerateRequest
import com.ceropapeleo.backend.logic.RequestValidator
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.receive
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.font.PDType1Font
import org.apache.pdfbox.pdmodel.font.Standard14Fonts
import java.io.ByteArrayOutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.ContentDisposition

@Serializable
data class SystemStatus(val status: String, val message: String)

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    // Configuración de Plugins
    install(ContentNegotiation) { json() }
    install(CallLogging)

    routing {

        // --- 1. ENDPOINT DE BIENVENIDA (Para ver en el navegador) ---
        get("/") {
            call.respondText("PRUEBA DE CONEXIÓN: OK ✅")
        }

        // --- 2. ENDPOINT DE SALUD
        get("/health") {
            call.respond(SystemStatus("OK", "Backend funcionando correctamente"))
        }

        // --- 3. ENDPOINT DE GENERACIÓN  ---
        post("/generate") {
            try {
                val request = call.receive<GenerateRequest>()
                val validator = RequestValidator()
                val fieldErrors = validator.validate(request)

                if (fieldErrors.isNotEmpty()) {
                    call.respond(
                        HttpStatusCode.BadRequest, ErrorResponse(
                            errorCode = "VALIDATION_ERROR",
                            message = "Existen errores en los datos enviados",
                            errors = fieldErrors
                        )
                    )
                    return@post
                }
                // --- NUEVA LÓGICA: GENERACIÓN REAL DEL PDF ---
                val pdfBytes = createDummyPdf(request)

                // Configuramos las cabeceras para que el cliente sepa que es un PDF
                call.response.header(
                    HttpHeaders.ContentDisposition,
                    ContentDisposition.Attachment.withParameter(ContentDisposition.Parameters.FileName, "Modelo790_Borrador.pdf").toString()
                )

                // Respondemos con los bytes del PDF
                call.respondBytes(pdfBytes, ContentType.Application.Pdf, HttpStatusCode.Created)

            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(
                    HttpStatusCode.InternalServerError, ErrorResponse(
                        errorCode = "INTERNAL_SERVER_ERROR",
                        message = "Error inesperado: ${e.message}",
                        errors = null
                    )
                )
            }
        }
    }
}
/**
 * Función que genera el contenido del PDF.
 * Se define fuera de module() para mantener el código organizado.
 */
private fun createDummyPdf(request: com.ceropapeleo.backend.dto.GenerateRequest): ByteArray {
    val outputStream = java.io.ByteArrayOutputStream()

    org.apache.pdfbox.pdmodel.PDDocument().use { document ->
        val page = org.apache.pdfbox.pdmodel.PDPage()
        document.addPage(page)

        org.apache.pdfbox.pdmodel.PDPageContentStream(document, page).use { content ->
            // Usamos fuentes estándar de PDFBox 3.x
            val fontTitle = org.apache.pdfbox.pdmodel.font.PDType1Font(org.apache.pdfbox.pdmodel.font.Standard14Fonts.FontName.HELVETICA_BOLD)
            val fontBody = org.apache.pdfbox.pdmodel.font.PDType1Font(org.apache.pdfbox.pdmodel.font.Standard14Fonts.FontName.HELVETICA)

            content.beginText()
            content.setFont(fontTitle, 20f)
            content.newLineAtOffset(50f, 750f)
            content.showText("CeroPapeleo - Borrador de Solicitud")
            content.endText()

            content.beginText()
            content.setFont(fontBody, 12f)
            content.newLineAtOffset(50f, 700f)
            content.setLeading(15f)

            content.showText("Solicitante: ${request.applicant.name} ${request.applicant.surname1}")
            content.newLine()
            content.showText("DNI/NIE: ${request.applicant.documentId}")
            content.newLine()
            content.showText("Tramite: ${request.certificateType}")
            content.newLine()

            val fecha = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))
            content.showText("Fecha de generacion: $fecha")
            content.endText()
        }
        document.save(outputStream)
    }
    return outputStream.toByteArray()
}