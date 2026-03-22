package com.unirfp.ceropapeleo.forms.sections

import androidx.compose.runtime.Composable
import com.unirfp.ceropapeleo.forms.components.CustomTextField
import com.unirfp.ceropapeleo.forms.components.DatePickerField
import com.unirfp.ceropapeleo.forms.components.FormSectionTitle
import com.unirfp.ceropapeleo.model.LastWillExtra

@Composable
fun LastWillExtraSection(
    lastWillExtra: LastWillExtra,
    willDateTouchedOrShowErrors: Boolean,
    willDateError: String?,
    todayMillis: Long,
    onLastWillExtraChange: (LastWillExtra) -> Unit,
    onWillDateTouched: () -> Unit,
    sanitizeLetters: (String, Int) -> String
) {
    // --- 5. DATOS ADICIONALES ---
    FormSectionTitle("6. Datos adicionales si los conoce")

    DatePickerField(
        value = lastWillExtra.willDate,
        label = "Fecha del testamento",
        isError = willDateTouchedOrShowErrors && willDateError != null,
        errorMessage = willDateError ?: "",
        onDateSelected = {
            onLastWillExtraChange(
                lastWillExtra.copy(willDate = it)
            )
            onWillDateTouched()
        },
        minDateMillis = null,
        maxDateMillis = todayMillis
    )

    CustomTextField(
        value = lastWillExtra.notary,
        onValueChange = {
            onLastWillExtraChange(
                lastWillExtra.copy(
                    notary = sanitizeLetters(it, 40)
                )
            )
        },
        label = "Notario",
        maxLength = 40
    )

    CustomTextField(
        value = lastWillExtra.grantPlace,
        onValueChange = {
            onLastWillExtraChange(
                lastWillExtra.copy(
                    grantPlace = sanitizeLetters(it, 40)
                )
            )
        },
        label = "Lugar de otorgamiento",
        maxLength = 40
    )

    CustomTextField(
        value = lastWillExtra.spousesFullName,
        onValueChange = {
            onLastWillExtraChange(
                lastWillExtra.copy(
                    spousesFullName = sanitizeLetters(it, 40)
                )
            )
        },
        label = "Apellidos y nombre cónyuge",
        maxLength = 40
    )
}