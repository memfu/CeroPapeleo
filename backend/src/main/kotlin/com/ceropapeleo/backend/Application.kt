package com.ceropapeleo.backend

import com.ceropapeleo.backend.dto.ErrorResponse
import com.ceropapeleo.backend.dto.GenerateRequest
import com.ceropapeleo.backend.logic.RequestValidator
import com.ceropapeleo.backend.services.PdfService
import com.ceropapeleo.backend.routes.pdfRoutes
import io.ktor.http.*
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
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Serializable
data class SystemStatus(val status: String, val message: String)

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    // 1. Configuración de Plugins Globales
    install(ContentNegotiation) {
        json()
    }
    install(CallLogging)

    // 2. Instanciamos el servicio de PDFBox (Inyección de dependencias manual)
    // Se crea aquí para que pueda ser compartido por todas las rutas
    val pdfService = PdfService()

    routing {

        // --- 3. TUS RUTAS (PDF Oficial: Rellenado e Inspección) ---
        // Al llamar a esta función, activamos /fill-pdf y /inspect-pdf
        pdfRoutes(pdfService)

        // --- ENDPOINT DE BIENVENIDA ---
        get("/") {
            call.respondText("CeroPapeleo Backend: Conexión establecida ✅")
        }

        // --- ENDPOINT DE SALUD (Health Check) ---
        get("/health") {
            call.respond(SystemStatus("OK", "Servidor funcionando correctamente"))
        }

        // --- GENERACIÓN DE BORRADOR (Lógica de Maria/Cris) ---
        post("/generate") {
            try {
                val request = call.receive<GenerateRequest>()
                val validator = RequestValidator()
                val fieldErrors = validator.validate(request)

                if (fieldErrors.isNotEmpty()) {
                    call.respond(
                        HttpStatusCode.BadRequest, ErrorResponse(
                            errorCode = "VALIDATION_ERROR",
                            message = "Existen errores en los datos enviados"
                        )
                    )
                    return@post
                }

                // Generación de PDF dummy (Borrador)
                val pdfBytes = createDummyPdf(request)

                call.response.header(
                    HttpHeaders.ContentDisposition,
                    ContentDisposition.Attachment.withParameter(ContentDisposition.Parameters.FileName, "Modelo790_Borrador.pdf").toString()
                )

                call.respondBytes(pdfBytes, ContentType.Application.Pdf, HttpStatusCode.Created)

            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(
                    HttpStatusCode.InternalServerError, ErrorResponse(
                        errorCode = "INTERNAL_SERVER_ERROR",
                        message = "Error inesperado: ${e.message}"
                    )
                )
            }
        }
    }
}

/**
 * Función que genera el contenido del PDF desde cero (Borrador).
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