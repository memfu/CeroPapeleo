package com.unirfp.ceropapeleo.forms.sections

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.unirfp.ceropapeleo.model.CertificateType

@Composable
fun CertificateTypeSection(
    selectedType: CertificateType,
    onTypeSelected: (CertificateType) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Tipo de certificado")

        RadioButtonWithLabel(
            selected = selectedType == CertificateType.CRIMINAL_RECORDS,
            label = "17 - Antecedentes Penales",
            onClick = { onTypeSelected(CertificateType.CRIMINAL_RECORDS) }
        )

        RadioButtonWithLabel(
            selected = selectedType == CertificateType.LAST_WILL,
            label = "18 - Últimas Voluntades",
            onClick = { onTypeSelected(CertificateType.LAST_WILL) }
        )

        RadioButtonWithLabel(
            selected = selectedType == CertificateType.LIFE_INSURANCE,
            label = "19 - Contrato de seguros de cobertura de fallecimiento",
            onClick = { onTypeSelected(CertificateType.LIFE_INSURANCE) }
        )
    }
}

@Composable
private fun RadioButtonWithLabel(
    selected: Boolean,
    label: String,
    onClick: () -> Unit
) {
    androidx.compose.foundation.layout.Row {
        RadioButton(
            selected = selected,
            onClick = onClick
        )
        Text(label)
    }
}