package com.example.carhive.Presentation.initial.Register.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.carhive.Presentation.initial.Register.viewModel.SecondRegisterViewModel

@Composable
fun SecondRegisterScreen(
    navigateToNext: () -> Unit,
    navigateToPrevious: () -> Unit,
    viewModel: SecondRegisterViewModel = hiltViewModel()
) {
    var curp by remember { mutableStateOf("") } // Nueva variable para CURP
    var phoneNumber by remember { mutableStateOf("") }
    var voterID by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(text = "CURP") // Texto para CURP
        TextField(
            value = curp,
            onValueChange = { curp = it },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "Phone Number")
        TextField(
            value = phoneNumber,
            onValueChange = { phoneNumber = it },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "Voter ID")
        TextField(
            value = voterID,
            onValueChange = { voterID = it },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            viewModel.saveSecondPartOfUserData(curp, phoneNumber, voterID )
            navigateToNext()
        }) {
            Text(text = "Next")
        }

        Text(
            text = "Go back",
            modifier = Modifier.clickable { navigateToPrevious() }
        )
    }
}
