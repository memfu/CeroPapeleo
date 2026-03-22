package com.unirfp.ceropapeleo.forms.sections

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.unirfp.ceropapeleo.forms.components.CustomTextField
import com.unirfp.ceropapeleo.forms.components.FormSectionTitle
import com.unirfp.ceropapeleo.model.Applicant
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ApplicantSection(
    applicant: Applicant,
    showErrors: Boolean,
    nameRequester: BringIntoViewRequester,
    firstSurnameRequester: BringIntoViewRequester,
    documentIdRequester: BringIntoViewRequester,
    onApplicantChange: (Applicant) -> Unit,
    sanitizeLetters: (String, Int) -> String
) {
    // --- 1. DATOS DEL SOLICITANTE ---
    FormSectionTitle("1. Datos del Solicitante")

    CustomTextField(
        value = applicant.name,
        onValueChange = {
            onApplicantChange(
                applicant.copy(name = sanitizeLetters(it, 30))
            )
        },
        label = "Nombre*",
        isError = showErrors && applicant.name.isBlank(),
        maxLength = 30,
        modifier = Modifier
            .fillMaxWidth()
            .bringIntoViewRequester(nameRequester)
    )

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        CustomTextField(
            value = applicant.firstSurname,
            onValueChange = {
                onApplicantChange(
                    applicant.copy(firstSurname = sanitizeLetters(it, 40))
                )
            },
            label = "1er apellido*",
            isError = showErrors && applicant.firstSurname.isBlank(),
            modifier = Modifier
                .weight(1f)
                .bringIntoViewRequester(firstSurnameRequester),
            maxLength = 40
        )

        CustomTextField(
            value = applicant.secondSurname,
            onValueChange = {
                onApplicantChange(
                    applicant.copy(secondSurname = sanitizeLetters(it, 40))
                )
            },
            label = "2do apellido",
            modifier = Modifier.weight(1f),
            maxLength = 40
        )
    }

    CustomTextField(
        value = applicant.documentId,
        onValueChange = {
            onApplicantChange(
                applicant.copy(documentId = it.trim().take(15))
            )
        },
        label = "DNI / NIE / Pasaporte*",
        isError = showErrors && applicant.documentId.isBlank(),
        maxLength = 15,
        modifier = Modifier
            .fillMaxWidth()
            .bringIntoViewRequester(documentIdRequester)
    )
}