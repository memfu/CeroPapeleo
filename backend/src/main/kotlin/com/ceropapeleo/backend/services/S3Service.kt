package com.ceropapeleo.backend.services

import aws.sdk.kotlin.services.s3.S3Client
import aws.sdk.kotlin.services.s3.model.PutObjectRequest
import aws.smithy.kotlin.runtime.content.ByteStream

// Singleton
object S3Service {

    // El cliente se crea una sola vez al inicio
    private val s3Client = S3Client { region = "eu-north-1" }

    private val bucketName = "ceropapeleo-s3"

    suspend fun uploadPdf(fileName: String, fileBytes: ByteArray): String? {
        // Definimos la carpeta una sola vez para no equivocarnos
        val folder = "Tramites"

        return try {
            s3Client.putObject(PutObjectRequest {
                bucket = bucketName
                key = "$folder/$fileName"
                body = ByteStream.fromBytes(fileBytes)
                contentType = "application/pdf"
            })

            "https://$bucketName.s3.eu-north-1.amazonaws.com/$folder/$fileName"

        } catch (e: Exception) {
            println("❌ Error subiendo a S3: ${e.message}")
            null
        }
    }
}