package com.ceropapeleo.backend.services

import aws.sdk.kotlin.services.dynamodb.DynamoDbClient
import aws.sdk.kotlin.services.dynamodb.model.AttributeValue
import aws.sdk.kotlin.services.dynamodb.model.PutItemRequest
import org.slf4j.LoggerFactory

class DynamoService {
    private val logger = LoggerFactory.getLogger("DynamoService")
    private val tableName = "Tramites"

    suspend fun saveTramite(dni: String, s3Url: String, tramiteTipo: String) {
        val client = DynamoDbClient { region = "us-east-1" }
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
            client.putItem(request)
            logger.info("🗄️ [DynamoDB] Registro guardado con éxito para el DNI: $dni")
        } catch (e: Exception) {
            logger.error("❌ [DynamoDB] Error al guardar registro: ${e.message}")
            throw e
        } finally {
            client.close()
        }
    }
}