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

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module).start(wait = true)
}

fun Application.module() {
    val logger = LoggerFactory.getLogger("Application")

    // 1. Plugins: Configuración de JSON y Logs
    install(ContentNegotiation) { json() }
    install(CallLogging)

    // 2. Dependencias: Instanciamos el servicio (el de la carpeta 'services' con S)
    val pdfService = PdfService()

    // 3. Rutas: Conectamos los módulos de endpoints
    routing {
        pdfRoutes(pdfService)
    }

    logger.info("🚀 Backend de CeroPapeleo arrancado correctamente")
}