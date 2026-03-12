package com.ceropapeleo.backend.routes

import com.ceropapeleo.backend.services.PdfService
import com.ceropapeleo.backend.logic.PdfMapper
import com.ceropapeleo.backend.dto.ErrorResponse
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.utils.io.readRemaining
import kotlinx.io.readByteArray
import kotlinx.serialization.json.Json

fun Route.pdfRoutes(pdfService: PdfService) {

    /**
     * TAREA 1: Inspeccionar campos de un PDF.
     */
    post("/inspect-pdf") {
        try {
            val multipart = call.receiveMultipart()
            var pdfBytes: ByteArray? = null

            multipart.forEachPart { part ->
                if (part is PartData.FileItem && part.name == "pdf_file") {
                    pdfBytes = part.provider().readRemaining().readByteArray()
                }
                part.dispose()
            }

            if (pdfBytes == null) {
                return@post call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse(errorCode = "NO_FILE", message = "No se ha recibido el archivo PDF.")
                )
            }

            val fields = pdfService.inspectPdfFields(pdfBytes!!.inputStream())
            call.respond(HttpStatusCode.OK, fields)

        } catch (e: Exception) {
            application.log.error("Error en /inspect-pdf", e)
            call.respond(
                HttpStatusCode.InternalServerError,
                ErrorResponse(errorCode = "SERVER_ERROR", message = e.message ?: "Error interno")
            )
        }
    }

    /**
     * TAREA 2: Rellenar PDF oficial con Mapping Inteligente.
     */
    post("/fill-pdf") {
        try {
            val multipart = call.receiveMultipart()
            var pdfBytes: ByteArray? = null
            var userDataJson: String? = null

            multipart.forEachPart { part ->
                when (part) {
                    is PartData.FileItem -> {
                        if (part.name == "pdf_file") pdfBytes = part.provider().readRemaining().readByteArray()
                    }
                    is PartData.FormItem -> {
                        if (part.name == "user_data") userDataJson = part.value
                    }
                    else -> {}
                }
                part.dispose()
            }

            if (pdfBytes == null || userDataJson == null) {
                return@post call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse(errorCode = "MISSING_DATA", message = "Falta el PDF o los datos JSON.")
                )
            }

            // Parseo y Mapping
            val rawData: Map<String, String> = Json.decodeFromString(userDataJson!!)
            val translatedData = PdfMapper.transformToPdfFields(rawData)

            // Procesado con PDFBox
            val filledPdfBytes = pdfService.fillPdfForm(pdfBytes!!.inputStream(), translatedData)

            call.respondBytes(filledPdfBytes, ContentType.Application.Pdf, HttpStatusCode.OK)

        } catch (e: Exception) {
            application.log.error("Error en /fill-pdf", e)
            call.respond(
                HttpStatusCode.InternalServerError,
                ErrorResponse(errorCode = "PROCESSING_ERROR", message = e.message ?: "Fallo al rellenar PDF")
            )
        }
    }
}