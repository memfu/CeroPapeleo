package com.unirfp.ceropapeleo.forms.utils

fun String.sanitizeLetters(max: Int): String =
    this.filter { it.isLetter() || it.isWhitespace() || it == '-' || it == '\'' }
        .take(max)

fun String.sanitizeDigits(max: Int): String =
    this.filter { it.isDigit() }.take(max)

fun String.sanitizeDate(max: Int = 10): String =
    this.filter { it.isDigit() || it == '/' }.take(max)

fun String.sanitizeAlphanumeric(max: Int): String =
    this.filter { it.isLetterOrDigit() }.take(max)