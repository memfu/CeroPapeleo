package com.ceropapeleo.backend.dto

import kotlinx.serialization.Serializable
import java.time.Instant

@Serializable
data class ErrorResponse(
    val status: String = "ERROR",
    val errorCode: String,
    val message: String,
    val errors: List<FieldError>? = null,
    val timestamp: String = Instant.now().toString()
)

@Serializable
data class FieldError(val field: String, val message: String)