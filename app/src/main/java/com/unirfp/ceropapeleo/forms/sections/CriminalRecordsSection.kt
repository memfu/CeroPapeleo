@file:OptIn(ExperimentalFoundationApi::class)

package com.unirfp.ceropapeleo.forms.sections

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unirfp.ceropapeleo.forms.components.CustomTextField
import com.unirfp.ceropapeleo.forms.components.DatePickerField
import com.unirfp.ceropapeleo.model.CriminalRecordsDetails

@Composable
fun CriminalRecordsSection(
    details: CriminalRecordsDetails,
    showErrors: Boolean,
    birthDateError: String?,
    documentRequester: BringIntoViewRequester,
    firstSurnameOrBusinessRequester: BringIntoViewRequester,
    nameRequester: BringIntoViewRequester,
    purposeRequester: BringIntoViewRequester,
    onDetailsChange: (CriminalRecordsDetails) -> Unit,
    onBirthDateTouched: () -> Unit,
    sanitizeLetters: (String, Int) -> String,
    sanitizeAlphanumeric: (String, Int) -> String,
    todayMillis: Long
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.sp.value.dp)
    ) {
        Text(
            text = "Datos específicos de antecedentes penales",
            fontSize = 16.sp
        )

        CustomTextField(
            value = details.subjectDocumentId,
            onValueChange = {
                onDetailsChange(
                    details.copy(subjectDocumentId = sanitizeAlphanumeric(it, 20))
                )
            },
            label = "NIF/CIF/NIE*",
            modifier = Modifier
                .fillMaxWidth()
                .bringIntoViewRequester(documentRequester),
            isError = showErrors && details.subjectDocumentId.isBlank()
        )

        CustomTextField(
            value = details.subjectFirstSurnameOrBusinessName,
            onValueChange = {
                onDetailsChange(
                    details.copy(
                        subjectFirstSurnameOrBusinessName = sanitizeLetters(it, 60)
                    )
                )
            },
            label = "1er apellido o denominación social*",
            modifier = Modifier
                .fillMaxWidth()
                .bringIntoViewRequester(firstSurnameOrBusinessRequester),
            isError = showErrors && details.subjectFirstSurnameOrBusinessName.isBlank()
        )

        CustomTextField(
            value = details.subjectSecondSurname,
            onValueChange = {
                onDetailsChange(
                    details.copy(subjectSecondSurname = sanitizeLetters(it, 60))
                )
            },
            label = "2do apellido",
            modifier = Modifier.fillMaxWidth()
        )

        CustomTextField(
            value = details.subjectName,
            onValueChange = {
                onDetailsChange(
                    details.copy(subjectName = sanitizeLetters(it, 60))
                )
            },
            label = "Nombre*",
            modifier = Modifier
                .fillMaxWidth()
                .bringIntoViewRequester(nameRequester),
            isError = showErrors && details.subjectName.isBlank()
        )

        DatePickerField(
            value = details.birthDate,
            label = "Fecha de nacimiento",
            isError = showErrors && birthDateError != null,
            errorMessage = birthDateError ?: "",
            onDateSelected = {
                onDetailsChange(details.copy(birthDate = it))
                onBirthDateTouched()
            },
            minDateMillis = null,
            maxDateMillis = todayMillis,
            modifier = Modifier.fillMaxWidth()
        )

        CustomTextField(
            value = details.birthCity,
            onValueChange = {
                onDetailsChange(
                    details.copy(birthCity = sanitizeLetters(it, 60))
                )
            },
            label = "Población de nacimiento",
            modifier = Modifier.fillMaxWidth()
        )

        CustomTextField(
            value = details.birthProvinceOrCountry,
            onValueChange = {
                onDetailsChange(
                    details.copy(birthProvinceOrCountry = sanitizeLetters(it, 60))
                )
            },
            label = "Provincia/País de nacimiento",
            modifier = Modifier.fillMaxWidth()
        )

        CustomTextField(
            value = details.nationalityCountry,
            onValueChange = {
                onDetailsChange(
                    details.copy(nationalityCountry = sanitizeLetters(it, 60))
                )
            },
            label = "País de nacionalidad",
            modifier = Modifier.fillMaxWidth()
        )

        CustomTextField(
            value = details.fatherName,
            onValueChange = {
                onDetailsChange(
                    details.copy(fatherName = sanitizeLetters(it, 60))
                )
            },
            label = "Nombre del padre",
            modifier = Modifier.fillMaxWidth()
        )

        CustomTextField(
            value = details.motherName,
            onValueChange = {
                onDetailsChange(
                    details.copy(motherName = sanitizeLetters(it, 60))
                )
            },
            label = "Nombre de la madre",
            modifier = Modifier.fillMaxWidth()
        )

        CustomTextField(
            value = details.purpose,
            onValueChange = {
                onDetailsChange(
                    details.copy(purpose = sanitizeLetters(it, 120))
                )
            },
            label = "Finalidad para la que se solicita*",
            modifier = Modifier
                .fillMaxWidth()
                .bringIntoViewRequester(purposeRequester),
            isError = showErrors && details.purpose.isBlank()
        )
    }
}