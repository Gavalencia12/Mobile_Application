package com.example.carhive.Presentation.initial.Register

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import coil.compose.rememberImagePainter
import com.example.carhive.ui.theme.Purple40
import com.example.carhive.ui.theme.SelectedField
import com.example.carhive.ui.theme.UnselectedField
import com.example.carhive.ui.theme.white
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

@Composable
fun SecondRegisterScreen(
    password: String,
    email: String,
    lastName: String,
    firstName: String,
    auth: FirebaseAuth,
    navigateToThirdRegister: (
        firstName: String,
        lastName: String,
        email: String,
        password: String,
        curp: String,
        voterID: String,
        phoneNumber: String) -> Unit,
) {

    var curp by remember { mutableStateOf("") }
    var voterID by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }

    var errorMessage by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Purple40)
            .padding(vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row() {
            Spacer(modifier = Modifier.weight(1f))
        }
        Text("curp", color = white, fontWeight = FontWeight.Bold, fontSize = 40.sp)
        TextField(
            value = curp,
            onValueChange = { curp = it },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = UnselectedField,
                focusedContainerColor = SelectedField
            )
        )
        Text("voterID", color = white, fontWeight = FontWeight.Bold, fontSize = 40.sp)
        TextField(
            value = voterID,
            onValueChange = { voterID = it },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = UnselectedField,
                focusedContainerColor = SelectedField
            )
        )
        Text("phoneNumber", color = white, fontWeight = FontWeight.Bold, fontSize = 40.sp)
        TextField(
            value = phoneNumber,
            onValueChange = { phoneNumber = it },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = UnselectedField,
                focusedContainerColor = SelectedField
            )
        )
        Spacer(Modifier.height(48.dp))
        Button(onClick = {
            if (curp.isNotEmpty() && voterID.isNotEmpty() && phoneNumber.isNotEmpty()) {
                navigateToThirdRegister(
                    firstName,
                    lastName,
                    email,
                    password,
                    curp,
                    voterID,
                    phoneNumber
                )
            } else {
                errorMessage = "Please fill in all field"
            }
        }) {
            Text("crear")
        }

        if (errorMessage.isNotEmpty()) {
            Text(
                text = errorMessage,
                color = Color.Red,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}
