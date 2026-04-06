package com.ceropapeleo.backend.services

import aws.sdk.kotlin.services.dynamodb.DynamoDbClient
import aws.sdk.kotlin.services.dynamodb.model.AttributeValue
import aws.sdk.kotlin.services.dynamodb.model.PutItemRequest
import org.slf4j.LoggerFactory

// Patrón Singleton
object DynamoService {
    private val logger = LoggerFactory.getLogger("DynamoService")
    private val tableName = "Tramites"

    // El cliente se crea UNA SOLA VEZ al arrancar el servidor
    private val client = DynamoDbClient { region = "eu-north-1" }

    suspend fun saveTramite(dni: String, s3Url: String, tramiteTipo: String) {
        val currentTime = System.currentTimeMillis().toString()

        val itemValues = mapOf(
            "dni" to AttributeValue.S(dni),
            "timestamp" to AttributeValue.S(currentTime),
            "s3Url" to AttributeValue.S(s3Url),
            "tipo" to AttributeValue.S(tramiteTipo)
        )

        val request = PutItemRequest {
            tableName = this@DynamoService.tableName
            item = itemValues
        }

        try {
            // Usamos el cliente que ya está conectado y listo
            client.putItem(request)
            logger.info("🗄️ [DynamoDB] Registro guardado con éxito para el DNI: $dni")
        } catch (e: Exception) {
            logger.error("❌ [DynamoDB] Error al guardar registro: ${e.message}")
            throw e
        }
    }
}