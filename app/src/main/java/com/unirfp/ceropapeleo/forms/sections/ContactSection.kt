package com.unirfp.ceropapeleo.forms.sections

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import com.unirfp.ceropapeleo.forms.components.CustomTextField
import com.unirfp.ceropapeleo.forms.components.FormSectionTitle
import com.unirfp.ceropapeleo.model.Contact

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ContactSection(
    contact: Contact,
    email: String,
    emailTouchedOrShowErrors: Boolean,
    isEmailValid: Boolean,
    emailRequester: BringIntoViewRequester,
    onContactChange: (Contact) -> Unit,
    onEmailBlur: () -> Unit,
    sanitizeDigits: (String, Int) -> String
) {
    // ---  MÉTODO DE RECEPCIÓN ---
    FormSectionTitle("2. ¿Dónde desea recibir su certificado?")

    CustomTextField(
        value = contact.mobilePhone,
        onValueChange = {
            onContactChange(
                contact.copy(
                    mobilePhone = sanitizeDigits(it, 15)
                )
            )
        },
        label = "Teléfono móvil (para recibir por SMS)",
        isError = false,
        maxLength = 15
    )

    CustomTextField(
        value = email,
        onValueChange = {
            onContactChange(
                contact.copy(email = it.take(60))
            )
        },
        label = "Correo electrónico (para recibir por EMAIL)",
        isError = emailTouchedOrShowErrors && !isEmailValid,
        errorMessage = "Introduce un email válido",
        onBlur = onEmailBlur,
        maxLength = 60,
        modifier = Modifier
            .fillMaxWidth()
            .bringIntoViewRequester(emailRequester)
    )
}