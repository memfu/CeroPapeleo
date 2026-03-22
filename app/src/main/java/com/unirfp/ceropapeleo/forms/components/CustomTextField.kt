package com.unirfp.ceropapeleo.forms.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.layout.fillMaxWidth

@Composable
fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isError: Boolean = false,
    errorMessage: String = "Campo obligatorio",
    maxLength: Int = Int.MAX_VALUE,
    onBlur: (() -> Unit)? = null,
    modifier: Modifier = Modifier.fillMaxWidth()
) {
    var hadFocus by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = value,
        onValueChange = {
            if (!it.contains("\n") && it.length <= maxLength) {
                onValueChange(it)
            }
        },
        label = { Text(label) },
        isError = isError,
        singleLine = true,
        maxLines = 1,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        supportingText = {
            if (isError) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        modifier = modifier.onFocusChanged { focusState ->
            if (focusState.isFocused) {
                hadFocus = true
            } else if (hadFocus) {
                onBlur?.invoke()
            }
        }
    )
}