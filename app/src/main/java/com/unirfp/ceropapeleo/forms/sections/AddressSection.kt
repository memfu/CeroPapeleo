package com.unirfp.ceropapeleo.forms.sections

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.unirfp.ceropapeleo.forms.components.CustomTextField
import com.unirfp.ceropapeleo.forms.components.FormSectionTitle
import com.unirfp.ceropapeleo.model.Address

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AddressSection(
    address: Address,
    showErrors: Boolean,
    postalCode: String,
    countryNormalized: String,
    isPostalCodeValid: Boolean,
    streetRequester: BringIntoViewRequester,
    postalCodeRequester: BringIntoViewRequester,
    cityRequester: BringIntoViewRequester,
    provinceRequester: BringIntoViewRequester,
    countryRequester: BringIntoViewRequester,
    onAddressChange: (Address) -> Unit,
    sanitizeLetters: (String, Int) -> String,
    sanitizeDigits: (String, Int) -> String,
    sanitizeAlphanumeric: (String, Int) -> String
) {

    // --- 3. DOMICILIO DEL SOLICITANTE ---
    FormSectionTitle("3. Domicilio del Solicitante")

    CustomTextField(
        value = address.street,
        onValueChange = {
            onAddressChange(
                address.copy(street = it.take(60))
            )
        },
        label = "Calle/Plaza/Avenida*",
        isError = showErrors && address.street.isBlank(),
        maxLength = 60,
        modifier = Modifier
            .fillMaxWidth()
            .bringIntoViewRequester(streetRequester)
    )

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = address.number,
            onValueChange = {
                onAddressChange(
                    address.copy(number = sanitizeDigits(it, 5))
                )
            },
            label = { Text("Nº") },
            modifier = Modifier.weight(1f)
        )

        OutlinedTextField(
            value = address.staircase,
            onValueChange = {
                onAddressChange(
                    address.copy(staircase = sanitizeAlphanumeric(it, 3))
                )
            },
            label = { Text("Esc.") },
            modifier = Modifier.weight(1f)
        )

        OutlinedTextField(
            value = address.floor,
            onValueChange = {
                onAddressChange(
                    address.copy(floor = sanitizeDigits(it, 3))
                )
            },
            label = { Text("Piso") },
            modifier = Modifier.weight(1f)
        )

        OutlinedTextField(
            value = address.door,
            onValueChange = {
                onAddressChange(
                    address.copy(door = sanitizeAlphanumeric(it, 3))
                )
            },
            label = { Text("Puerta") },
            modifier = Modifier.weight(1f)
        )
    }

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {

        CustomTextField(
            value = address.postalCode,
            onValueChange = {
                val sanitizedPostalCode = when (countryNormalized) {
                    "españa", "spain" -> it.filter { c -> c.isDigit() }.take(5)
                    else -> it.filter { c -> c.isLetterOrDigit() || c == ' ' || c == '-' }.take(10)
                }

                onAddressChange(
                    address.copy(postalCode = sanitizedPostalCode)
                )
            },
            label = "C.P.*",
            isError = showErrors && !isPostalCodeValid,
            errorMessage = when {
                postalCode.isBlank() -> "Campo obligatorio"
                countryNormalized in listOf("españa", "spain") && postalCode.length != 5 ->
                    "Debe tener 5 dígitos"
                else -> "Código postal inválido"
            },
            modifier = Modifier
                .weight(1f)
                .bringIntoViewRequester(postalCodeRequester),
            maxLength = Int.MAX_VALUE
        )

        CustomTextField(
            value = address.city,
            onValueChange = {
                onAddressChange(
                    address.copy(city = sanitizeLetters(it, 40))
                )
            },
            label = "Municipio*",
            isError = showErrors && address.city.isBlank(),
            modifier = Modifier
                .weight(2f)
                .bringIntoViewRequester(cityRequester),
            maxLength = 40
        )
    }

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {

        CustomTextField(
            value = address.province,
            onValueChange = {
                onAddressChange(
                    address.copy(province = sanitizeLetters(it, 40))
                )
            },
            label = "Provincia*",
            isError = showErrors && address.province.isBlank(),
            modifier = Modifier
                .weight(2f)
                .bringIntoViewRequester(provinceRequester),
            maxLength = 40
        )

        CustomTextField(
            value = address.country,
            onValueChange = {
                onAddressChange(
                    address.copy(country = sanitizeLetters(it, 40))
                )
            },
            label = "País*",
            isError = showErrors && address.country.isBlank(),
            modifier = Modifier
                .weight(2f)
                .bringIntoViewRequester(countryRequester),
            maxLength = 40
        )
    }
}