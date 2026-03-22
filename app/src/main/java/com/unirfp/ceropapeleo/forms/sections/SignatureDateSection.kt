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
import androidx.compose.ui.unit.sp
import com.unirfp.ceropapeleo.forms.components.CustomTextField
import com.unirfp.ceropapeleo.forms.components.DatePickerField
import com.unirfp.ceropapeleo.forms.components.FormSectionTitle
import com.unirfp.ceropapeleo.model.Signature
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SignatureDateSection(
    signature: Signature,
    showErrors: Boolean,
    isSignatureDateValid: Boolean,
    signatureDateError: String?,
    signatureRequester: BringIntoViewRequester,
    signaturePlaceRequester: BringIntoViewRequester,
    onSignatureChange: (Signature) -> Unit,
    sanitizeLetters: (String, Int) -> String,
    oneYearFromTodayMillis: Long
){
    // --- APARTADO: LUGAR Y FECHA DE LA FIRMA ---
    FormSectionTitle("8. Lugar y fecha de la firma")

    CustomTextField(
        value = signature.place,
        onValueChange = {
            onSignatureChange(
                signature.copy(
                    place = sanitizeLetters(it, 40)
                )
            )
        },
        label = "Ciudad de la firma*",
        isError = showErrors && signature.place.isBlank(),
        maxLength = 40,
        modifier = Modifier
            .fillMaxWidth()
            .bringIntoViewRequester(signaturePlaceRequester)
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .bringIntoViewRequester(signatureRequester),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text("a", fontSize = 14.sp)

        DatePickerField(
            value = signature.date,
            label = "Fecha de firma*",
            isError = showErrors && signatureDateError != null,
            errorMessage = signatureDateError ?: "Seleccione una fecha válida",
            onDateSelected = {
                onSignatureChange(
                    signature.copy(date = it)
                )
            },
            minDateMillis = null,
            maxDateMillis = oneYearFromTodayMillis,
            modifier = Modifier.weight(1f)
        )
    }
}