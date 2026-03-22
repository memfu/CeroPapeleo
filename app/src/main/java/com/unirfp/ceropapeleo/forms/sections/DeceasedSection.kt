package com.unirfp.ceropapeleo.forms.sections

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.unirfp.ceropapeleo.forms.components.CustomTextField
import com.unirfp.ceropapeleo.forms.components.DatePickerField
import com.unirfp.ceropapeleo.forms.components.FormSectionTitle
import com.unirfp.ceropapeleo.model.Deceased

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DeceasedSection(
    deceased: Deceased,
    showErrors: Boolean,
    birthDateTouchedOrShowErrors: Boolean,
    deathDateTouchedOrShowErrors: Boolean,
    birthDateError: String?,
    deathDateError: String?,
    todayMillis: Long,
    deceasedNameRequester: BringIntoViewRequester,
    deceasedFirstSurnameRequester: BringIntoViewRequester,
    deathDateRequester: BringIntoViewRequester,
    onDeceasedChange: (Deceased) -> Unit,
    onBirthDateTouched: () -> Unit,
    onDeathDateTouched: () -> Unit,
    sanitizeLetters: (String, Int) -> String
) {
    // --- 4. DATOS DEL FALLECIDO ---
    FormSectionTitle("5. Datos del Fallecido")

    CustomTextField(
        value = deceased.name,
        onValueChange = {
            onDeceasedChange(
                deceased.copy(name = sanitizeLetters(it, 30))
            )
        },
        label = "Nombre del fallecido*",
        isError = showErrors && deceased.name.isBlank(),
        maxLength = 30,
        modifier = Modifier
            .fillMaxWidth()
            .bringIntoViewRequester(deceasedNameRequester)
    )

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        CustomTextField(
            value = deceased.firstSurname,
            onValueChange = {
                onDeceasedChange(
                    deceased.copy(firstSurname = sanitizeLetters(it, 30))
                )
            },
            label = "1er apellido*",
            isError = showErrors && deceased.firstSurname.isBlank(),
            maxLength = 30,
            modifier = Modifier
                .weight(1f)
                .bringIntoViewRequester(deceasedFirstSurnameRequester)
        )

        CustomTextField(
            value = deceased.secondSurname,
            onValueChange = {
                onDeceasedChange(
                    deceased.copy(secondSurname = sanitizeLetters(it, 30))
                )
            },
            label = "2do apellido",
            isError = false,
            maxLength = 30,
            modifier = Modifier.weight(1f)
        )
    }

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        DatePickerField(
            value = deceased.birthDate,
            label = "Fecha de nacimiento",
            isError = birthDateTouchedOrShowErrors && birthDateError != null,
            errorMessage = birthDateError ?: "",
            onDateSelected = {
                onDeceasedChange(
                    deceased.copy(birthDate = it)
                )
                onBirthDateTouched()
            },
            minDateMillis = null,
            maxDateMillis = todayMillis,
            modifier = Modifier.weight(1f)
        )

        CustomTextField(
            value = deceased.birthCity,
            onValueChange = {
                onDeceasedChange(
                    deceased.copy(birthCity = sanitizeLetters(it, 40))
                )
            },
            label = "Población de nacimiento",
            modifier = Modifier.weight(1f),
            maxLength = 40
        )
    }

    CustomTextField(
        value = deceased.documentId,
        onValueChange = {
            onDeceasedChange(
                deceased.copy(documentId = it.trim().take(15))
            )
        },
        label = "NIF/NIE/Pasaporte",
        isError = false,
        maxLength = 15
    )

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        DatePickerField(
            value = deceased.deathDate,
            label = "Fecha de defunción*",
            isError = deathDateTouchedOrShowErrors && deathDateError != null,
            errorMessage = deathDateError ?: "",
            onDateSelected = {
                onDeceasedChange(
                    deceased.copy(deathDate = it)
                )
                onDeathDateTouched()
            },
            minDateMillis = null,
            maxDateMillis = todayMillis,
            modifier = Modifier
                .weight(1f)
                .bringIntoViewRequester(deathDateRequester)
        )

        CustomTextField(
            value = deceased.deathCity,
            onValueChange = {
                onDeceasedChange(
                    deceased.copy(deathCity = sanitizeLetters(it, 40))
                )
            },
            label = "Población de defunción",
            modifier = Modifier.weight(1f),
            maxLength = 40
        )
    }
}