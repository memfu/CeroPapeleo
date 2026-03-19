package com.ceropapeleo.backend.routes

import com.ceropapeleo.backend.services.PdfService
import com.ceropapeleo.backend.logic.PdfMapper
import com.ceropapeleo.backend.dto.ErrorResponse
import com.ceropapeleo.backend.dto.FieldError
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.utils.io.readRemaining
import kotlinx.io.readByteArray
import java.io.File

fun Route.pdfRoutes(pdfService: PdfService) {

    /**
     * TAREA 1: Inspección de campos (vía archivo físico)
     */
    post("/inspect-pdf") {
        val multipart = call.receiveMultipart()
        var pdfBytes: ByteArray? = null

        multipart.forEachPart { part ->
            if (part is PartData.FileItem) {
                pdfBytes = part.provider().readRemaining().readByteArray()
            }
            part.dispose()
        }

        if (pdfBytes == null) {
            return@post call.respond(HttpStatusCode.BadRequest, ErrorResponse(errorCode = "NO_FILE", message = "Falta el PDF"))
        }

        val fields = pdfService.inspectPdfFields(pdfBytes!!.inputStream())
        call.respond(HttpStatusCode.OK, fields)
    }

    /**
     * TAREA 2: Rellenar PDF externo (vía Multipart)
     */
    post("/fill-pdf") {
        // En desarrollo si se necesitara para otros modelos
        call.respond(HttpStatusCode.NotImplemented, "Utilizar /generate para el Modelo 790 oficial")
    }

    /**
     * TAREA 3: Generación Automática Modelo 790 (La que usa Cris desde el Frontend)
     */
    post("/generate") {
        try {
            val userData = call.receive<Map<String, String>>()

            // Validación de campos
            val obligatorios = listOf("documentId", "name", "surname1", "city", "postalCode")
            val faltantes = obligatorios.filter { userData[it].isNullOrBlank() }

            if (faltantes.isNotEmpty()) {
                val detalles = faltantes.map { FieldError(it, "Campo obligatorio") }
                return@post call.respond(HttpStatusCode.BadRequest, ErrorResponse(
                    errorCode = "VALIDATION_ERROR",
                    message = "Faltan datos obligatorios",
                    errors = detalles
                ))
            }

            val templateFile = File("formulario-790-006_es_es.pdf")
            if (!templateFile.exists()) {
                return@post call.respond(HttpStatusCode.InternalServerError, ErrorResponse(errorCode = "FILE_NOT_FOUND", message = "Plantilla PDF ausente"))
            }

            // Mapeo y generación
            val translatedData = PdfMapper.transformToPdfFields(userData)
            val pdfResult = pdfService.fillPdfForm(templateFile.inputStream(), translatedData)

            call.response.header(HttpHeaders.ContentDisposition, "attachment; filename=\"Modelo790.pdf\"")
            call.respondBytes(pdfResult, ContentType.Application.Pdf, HttpStatusCode.Created)

        } catch (e: Exception) {
            call.respond(HttpStatusCode.InternalServerError, ErrorResponse(errorCode = "SERVER_ERROR", message = e.message ?: "Error desconocido"))
        }
    }
}