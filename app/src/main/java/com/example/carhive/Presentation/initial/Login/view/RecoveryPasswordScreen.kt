package com.example.carhive.Presentation.initial.Login.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.carhive.Presentation.initial.Login.viewModel.EmailState
import com.example.carhive.Presentation.initial.Login.viewModel.RecoveryPasswordViewModel

@Composable
fun RecoveryPasswordScreen(

    viewModel: RecoveryPasswordViewModel = hiltViewModel(),
    navigateToLogin: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    val emailState by viewModel.emailState.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Recuperar contraseña")

        Spacer(modifier = Modifier.height(16.dp))

        // Campo de texto para ingresar el correo
        BasicTextField(
            value = email,
            onValueChange = { email = it },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            decorationBox = { innerTextField ->
                Box(
                    Modifier.padding(8.dp)
                ) {
                    if (email.isEmpty()) {
                        Text("Ingresa tu correo")
                    }
                    innerTextField()
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Botón para enviar el correo de recuperación
        Button(
            onClick = { viewModel.sendPasswordResetEmail(email) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Enviar correo de recuperación")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Mostrar el estado actual de la solicitud
        when (emailState) {
            is EmailState.Loading -> CircularProgressIndicator()
            is EmailState.Success -> {
                Text(text = "Correo enviado con éxito")
                LaunchedEffect(Unit) {
                    // Navega de vuelta al login después de un retraso
                    kotlinx.coroutines.delay(2000)
                    navigateToLogin()
                }
            }
            is EmailState.Error -> Text(text = (emailState as EmailState.Error).message)
            else -> {}
        }
    }

}