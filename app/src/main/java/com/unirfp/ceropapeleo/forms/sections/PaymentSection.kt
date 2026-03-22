package com.unirfp.ceropapeleo.forms.sections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unirfp.ceropapeleo.forms.components.CustomTextField
import com.unirfp.ceropapeleo.forms.components.FormSectionTitle
import com.unirfp.ceropapeleo.model.Payment

@Composable
fun PaymentSection(
    payment: Payment,
    showErrors: Boolean,
    onPaymentChange: (Payment) -> Unit
) {
    // --- SECCIÓN INGRESO Y FORMA DE PAGO ---
    FormSectionTitle("9. Ingreso y forma de pago")

    OutlinedTextField(
        value = "3,86",
        onValueChange = { },
        label = { Text("IMPORTE euros") },
        readOnly = true,
        suffix = { Text("€") },
        modifier = Modifier.width(180.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color.LightGray.copy(alpha = 0.2f),
            unfocusedContainerColor = Color.LightGray.copy(alpha = 0.2f)
        )
    )

    Spacer(modifier = Modifier.height(8.dp))
    Text("Forma de pago:", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)

    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(
                selected = payment.paymentMethod == "CASH",
                onClick = {
                    onPaymentChange(
                        payment.copy(paymentMethod = "CASH")
                    )
                }
            )
            Text("Efectivo")
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(
                selected = payment.paymentMethod == "ACCOUNT",
                onClick = {
                    onPaymentChange(
                        payment.copy(paymentMethod = "ACCOUNT")
                    )
                }
            )
            Text("E.C. adeudo en cuenta")
        }
    }

    if (payment.paymentMethod == "ACCOUNT") {
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Número de cuenta bancaria:",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.primary
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.Top
        ) {
            CustomTextField(
                value = payment.bankEnt,
                onValueChange = {
                    if (it.length <= 4 && it.all { c -> c.isDigit() }) {
                        onPaymentChange(payment.copy(bankEnt = it))
                    }
                },
                label = "Ent.*",
                modifier = Modifier.weight(1f),
                isError = showErrors && payment.paymentMethod == "ACCOUNT" && payment.bankEnt.length < 4,
                errorMessage = "4 dígitos",
                maxLength = 4
            )

            CustomTextField(
                value = payment.bankOff,
                onValueChange = {
                    if (it.length <= 4 && it.all { c -> c.isDigit() }) {
                        onPaymentChange(payment.copy(bankOff = it))
                    }
                },
                label = "Ofic.*",
                modifier = Modifier.weight(1f),
                isError = showErrors && payment.paymentMethod == "ACCOUNT" && payment.bankOff.length < 4,
                errorMessage = "4 dígitos",
                maxLength = 4
            )

            CustomTextField(
                value = payment.bankDC,
                onValueChange = {
                    if (it.length <= 2 && it.all { c -> c.isDigit() }) {
                        onPaymentChange(payment.copy(bankDC = it))
                    }
                },
                label = "DC*",
                modifier = Modifier.weight(0.8f),
                isError = showErrors && payment.paymentMethod == "ACCOUNT" && payment.bankDC.length < 2,
                errorMessage = "2 dígitos",
                maxLength = 2
            )

            CustomTextField(
                value = payment.bankAcc,
                onValueChange = {
                    if (it.length <= 10 && it.all { c -> c.isDigit() }) {
                        onPaymentChange(payment.copy(bankAcc = it))
                    }
                },
                label = "Cuenta*",
                modifier = Modifier.weight(1.8f),
                isError = showErrors && payment.paymentMethod == "ACCOUNT" && payment.bankAcc.length < 10,
                errorMessage = "10 dígitos",
                maxLength = 10
            )
        }
    }
}