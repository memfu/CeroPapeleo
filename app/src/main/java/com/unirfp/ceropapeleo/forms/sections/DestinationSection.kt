package com.unirfp.ceropapeleo.forms.sections

import androidx.compose.runtime.Composable
import com.unirfp.ceropapeleo.forms.components.CustomTextField
import com.unirfp.ceropapeleo.forms.components.FormSectionTitle
import com.unirfp.ceropapeleo.model.Destination

@Composable
fun DestinationSection(
    destination: Destination,
    onDestinationChange: (Destination) -> Unit,
    sanitizeLetters: (String, Int) -> String
) {
    // --- APARTADO: EFECTOS EN EL EXTRANJERO ---
    FormSectionTitle("4. Rellene solo si el certificado ha de tener efectos en el extranjero")

    CustomTextField(
        value = destination.country,
        onValueChange = {
            onDestinationChange(
                destination.copy(country = sanitizeLetters(it, 40))
            )
        },
        label = "País de destino (opcional)",
        isError = false,
        maxLength = 40
    )

    CustomTextField(
        value = destination.authorityOrEntity,
        onValueChange = {
            onDestinationChange(
                destination.copy(authorityOrEntity = it.take(60))
            )
        },
        label = "Entidad ante la que tiene que surtir efectos (opcional)",
        isError = false,
        maxLength = 60
    )
}