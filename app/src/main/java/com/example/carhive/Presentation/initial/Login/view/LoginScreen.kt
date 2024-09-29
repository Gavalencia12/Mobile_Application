package com.example.carhive.Presentation.initial.Login.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.carhive.Presentation.initial.Login.viewModel.LoginViewModel
import com.example.carhive.ui.theme.Purple40
import com.example.carhive.ui.theme.SelectedField
import com.example.carhive.ui.theme.UnselectedField
import com.example.carhive.ui.theme.white

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = hiltViewModel(),
    navigateToUser: () -> Unit = {},
    navigateToRegister: () -> Unit = {}
) {
    var email: String by remember { mutableStateOf("") }
    var password: String by remember { mutableStateOf("") }
    var errorMessage: String by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Purple40)
            .padding(vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Emxail", color = white, fontWeight = FontWeight.Bold, fontSize = 40.sp)
        TextField(
            value = email,
            onValueChange = { email = it },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = UnselectedField,
                focusedContainerColor = SelectedField
            )
        )
        Spacer(modifier = Modifier.height(40.dp))
        Text("Password", color = white, fontWeight = FontWeight.Bold, fontSize = 40.sp)
        TextField(
            value = password,
            onValueChange = { password = it },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = UnselectedField,
                focusedContainerColor = SelectedField
            )
        )
        Spacer(Modifier.height(48.dp))
        Button(onClick = {
            viewModel.onLogicClick(email, password, navigateToUser)
        }) {
            Text("Login")
        }
        if (errorMessage.isNotEmpty()){
            Text(errorMessage, color = Color.Red)
        }
        Spacer(Modifier.height(40.dp))
        Text(
            text = "Register Now",
            color = Color.White,
            modifier = Modifier.padding(24.dp).clickable { navigateToRegister() },
            fontWeight = FontWeight.Bold
        )

    }
}