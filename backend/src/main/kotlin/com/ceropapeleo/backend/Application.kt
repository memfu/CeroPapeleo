package com.ceropapeleo.backend

import com.ceropapeleo.backend.services.PdfService
import com.ceropapeleo.backend.routes.pdfRoutes
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.routing.*
import org.slf4j.LoggerFactory
import org.apache.pdfbox.pdmodel.font.PDType1Font
import org.apache.pdfbox.pdmodel.font.Standard14Fonts.FontName

fun main() {
    println("☕ Iniciando pre-calentamiento de motores PDF...")

    try {
        // Al llamar a PDType1Font con el Enum directo, PDFBox
        // se ve obligado a escanear todo el sistema de archivos.
        PDType1Font(FontName.HELVETICA)

        println("✅ Motores PDF listos. Cache de fuentes sincronizada.")
    } catch (_: Exception) {
        println("ℹ️  Escaneo de fuentes finalizado.")
    }

    // ARRANQUE DEL SERVIDOR
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module).start(wait = true)
}

fun Application.module() {
    val logger = LoggerFactory.getLogger("Application")

    install(ContentNegotiation) { json() }
    install(CallLogging)

    val pdfService = PdfService()

    routing {
        pdfRoutes(pdfService)
    }

    logger.info("🚀 Backend de CeroPapeleo escuchando peticiones en el puerto 8080")
}