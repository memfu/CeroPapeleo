package com.ceropapeleo.backend.services

import aws.sdk.kotlin.services.s3.S3Client
import aws.sdk.kotlin.services.s3.model.PutObjectRequest
import aws.smithy.kotlin.runtime.content.ByteStream

class S3Service {
    // El cliente usará automáticamente las variables de entorno de AWS
    private val s3Client = S3Client { region = "us-east-1" }

    // El nombre del bucket en la consola
    private val bucketName = "ceropapeleo-pdfs-marilu"

    suspend fun uploadPdf(fileName: String, fileBytes: ByteArray): String? {
        return try {
            s3Client.putObject(PutObjectRequest {
                bucket = bucketName
                key = "tramites/$fileName"
                body = ByteStream.fromBytes(fileBytes)
                contentType = "application/pdf"
            })

            // Construimos la URL pública para Cris
            "https://$bucketName.s3.amazonaws.com/tramites/$fileName"
        } catch (e: Exception) {
            println("❌ Error subiendo a S3: ${e.message}")
            null
        }
    }
}