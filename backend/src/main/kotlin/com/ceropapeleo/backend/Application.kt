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
            call.respondText("¡Servidor de CeroPapeleo operativo! 🚀\n\nEl backend está listo para recibir peticiones POST en /generate")
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

                // Si pasa la validación (Simulación de éxito)
                call.respond(
                    HttpStatusCode.Created, mapOf(
                        "status" to "SUCCESS",
                        "message" to "PDF generado correctamente"
                    )
                )

            } catch (e: Exception) {
                // Error 500 estándar
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