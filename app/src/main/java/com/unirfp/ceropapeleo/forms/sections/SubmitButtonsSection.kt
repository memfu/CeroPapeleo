package com.unirfp.ceropapeleo.forms.sections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun SubmitButtonsSection(
    navController: NavController,
    isSubmitting: Boolean,
    onSubmit: () -> Unit,
    primaryButtonText: String = "GENERAR PDF",
    secondaryButtonText: String = "VOLVER ATRÁS",
    onSecondaryClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        // ---------------------------------------------------------
        // BOTÓN SECUNDARIO (volver)
        // ---------------------------------------------------------
        Button(
            onClick = {
                onSecondaryClick?.invoke() ?: navController.popBackStack()
            },
            modifier = Modifier.weight(1f),
            shape = MaterialTheme.shapes.medium,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFF9800),
                contentColor = Color.White
            ),
            enabled = !isSubmitting
        ) {
            Text(secondaryButtonText)
        }

        // ---------------------------------------------------------
        // BOTÓN PRINCIPAL (acción)
        // ---------------------------------------------------------
        Button(
            onClick = onSubmit,
            modifier = Modifier.weight(1f),
            shape = MaterialTheme.shapes.medium,
            enabled = !isSubmitting
        ) {
            if (isSubmitting) {
                CircularProgressIndicator(
                    strokeWidth = 2.dp
                )
            } else {
                Text(primaryButtonText)
            }
        }
    }
}