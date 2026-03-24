package com.unirfp.ceropapeleo.forms.sections

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.sp
import com.unirfp.ceropapeleo.forms.components.FormSectionTitle
import com.unirfp.ceropapeleo.forms.components.SignaturePad
import com.unirfp.ceropapeleo.forms.components.SignaturePadView
import com.unirfp.ceropapeleo.model.Signature

@Composable
fun SignatureSection(
    signature: Signature,
    onSignatureChange: (Signature) -> Unit,
    onPadReady: (SignaturePadView) -> Unit,
    onSignatureCleared: () -> Unit
) {
    // --- 6. FIRMA ---
    FormSectionTitle("7. Firma y Autorización")

    Row (
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ){
        Checkbox(
            checked = signature.postalDeliveryAuthorized,
            onCheckedChange = {
                onSignatureChange(
                    signature.copy(postalDeliveryAuthorized = it)
                )
            }
        )
        Text("Autorizo el envío por correo postal", fontSize = 14.sp)
    }

    SignaturePad(
        onSignatureChanged = {
            // No guardamos Base64 aquí (se hace al generar PDF)
        },
        onSignatureCleared = onSignatureCleared,
        onPadReady = onPadReady
    )
}