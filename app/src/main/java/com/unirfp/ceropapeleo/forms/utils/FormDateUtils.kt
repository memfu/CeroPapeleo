package com.unirfp.ceropapeleo.forms.utils

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.ResolverStyle

val spanishDateFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("dd/MM/uuuu")
        .withResolverStyle(ResolverStyle.STRICT)

fun String.toLocalDateOrNull(): LocalDate? {
    return try {
        LocalDate.parse(this, spanishDateFormatter)
    } catch (e: Exception) {
        null
    }
}

fun String.isValidSpanishDate(): Boolean =
    this.toLocalDateOrNull() != null