package com.unirfp.ceropapeleo.forms


import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavController

@Composable
fun GenerateFormScreen(navController: NavController) {
    Button(
        onClick = { navController.navigate("webview") }
    ) {
        Text("Ir a la sede electrónica")
    }
}