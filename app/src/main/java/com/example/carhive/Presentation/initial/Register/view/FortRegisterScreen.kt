package com.example.carhive.Presentation.initial.Register.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.delay

@Composable
fun FortRegisterScreen(
    viewModel: FortRegisterViewModel = hiltViewModel(), // Usamos el nuevo ViewModel
    navigateToNext: () -> Unit
) {
    val isEmailVerified by viewModel.isEmailVerified.observeAsState(false)

    // Llamada a verificar el correo cada ciertos segundos
    LaunchedEffect(Unit) {
        while (!isEmailVerified) {
            delay(3000) // Verifica cada 3 segundos
            viewModel.checkEmailVerification()
        }
    }

    Column(
        modifier = androidx.compose.ui.Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (isEmailVerified) {
            // Botón habilitado cuando el correo es verificado
            Text("Correo verificado con éxito")
            Button(onClick = {
                navigateToNext()
            }, enabled = true
            ) {
                Text("Continuar")
            }
        } else {
            // Mostrar animación de carga y botón deshabilitado
            CircularProgressIndicator()
            Text("Esperando verificación de correo...")
            Button(onClick = {}, enabled = false) {
                Text("Continuar")
            }
        }
    }
}
