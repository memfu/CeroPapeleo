@file:OptIn(ExperimentalFoundationApi::class)

package com.unirfp.ceropapeleo.forms

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

data class FormRequesters(
    // --- 1. SOLICITANTE ---
    val name: BringIntoViewRequester,
    val firstSurname: BringIntoViewRequester,
    val documentId: BringIntoViewRequester,

    // --- 2. CONTACTO ---
    val email: BringIntoViewRequester,

    // --- 3. DIRECCIÓN ---
    val street: BringIntoViewRequester,
    val postalCode: BringIntoViewRequester,
    val city: BringIntoViewRequester,
    val province: BringIntoViewRequester,
    val country: BringIntoViewRequester,

    // --- 4. FALLECIDO ---
    val deceasedName: BringIntoViewRequester,
    val deceasedFirstSurname: BringIntoViewRequester,
    val deathDate: BringIntoViewRequester,

    // --- 5. FIRMA ---
    val signaturePlace: BringIntoViewRequester,
    val signature: BringIntoViewRequester
)

@Composable
fun rememberFormRequesters(): FormRequesters {
    return FormRequesters(
        // --- 1. SOLICITANTE ---
        name = remember { BringIntoViewRequester() },
        firstSurname = remember { BringIntoViewRequester() },
        documentId = remember { BringIntoViewRequester() },

        // --- 2. CONTACTO ---
        email = remember { BringIntoViewRequester() },

        // --- 3. DIRECCIÓN ---
        street = remember { BringIntoViewRequester() },
        postalCode = remember { BringIntoViewRequester() },
        city = remember { BringIntoViewRequester() },
        province = remember { BringIntoViewRequester() },
        country = remember { BringIntoViewRequester() },

        // --- 4. FALLECIDO ---
        deceasedName = remember { BringIntoViewRequester() },
        deceasedFirstSurname = remember { BringIntoViewRequester() },
        deathDate = remember { BringIntoViewRequester() },

        // --- 5. FIRMA ---
        signaturePlace = remember { BringIntoViewRequester() },
        signature = remember { BringIntoViewRequester() }
    )
}