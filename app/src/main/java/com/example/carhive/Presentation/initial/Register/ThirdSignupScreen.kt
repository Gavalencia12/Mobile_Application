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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import com.example.carhive.ui.theme.Purple40
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

@Composable
fun ThirdSignupScreen(
    firstName: String,
    lastName: String,
    email: String,
    password: String,
    curp: String,
    voterID: String,
    phoneNumber: String,
    auth: FirebaseAuth,
    navigateToAdmin: () -> Unit,
) {
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var errorMessage by remember { mutableStateOf("") }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri -> imageUri = uri }
    )
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
        Button(onClick = { launcher.launch("image/*") }) {
            Text("Select Profile Image")
        }
        Spacer(Modifier.height(48.dp))
        imageUri?.let { uri ->
            Image(
                painter = rememberImagePainter(uri),
                contentDescription = "Selected Image",
                modifier = Modifier.size(100.dp)
            )
        }
        Button(onClick = {
            if (imageUri != null) {
                // Llamada a la función para completar el registro
                uploadImageToStorage(
                    auth = auth,
                    firstName = firstName,
                    lastName = lastName,
                    email = email,
                    password = password,
                    curp = curp,
                    phoneNumber = phoneNumber,
                    voterID = voterID,
                    imageUri = imageUri!!,
                    navigateToAdmin = navigateToAdmin,
                    onFailure = { errorMessage = it.toString() },
                )
            } else {
                errorMessage = "Please fill in all fields and select an image"
            }
        }) {
            Text("Create Account")
        }
    }
}

fun uploadImageToStorage(
    auth: FirebaseAuth,
    email: String,
    password: String,
    imageUri: Uri,
    onFailure: (String?) -> Unit,
    lastName: String,
    firstName: String,
    voterID: String,
    phoneNumber: String,
    curp: String,
    navigateToAdmin: () -> Unit,
) {
    auth.createUserWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val user = auth.currentUser
                val userId = user?.uid

                if (userId != null) {
                    val storageRef =
                        FirebaseStorage.getInstance().getReference("Users/$userId/profile.jpg")
                    storageRef.putFile(imageUri)
                        .addOnSuccessListener { taskSnapshot ->
                            taskSnapshot.storage.downloadUrl.addOnSuccessListener { uri ->
                                val imageUrl = uri.toString()

                                saveUserToDatabase(
                                    userId = userId,
                                    firstName = firstName,
                                    lastName = lastName,
                                    email = email,
                                    curp = curp,
                                    phoneNumber = phoneNumber,
                                    voterID = voterID,
                                    imageUrl = imageUrl,
                                    onFailure = onFailure,
                                    navigateToAdmin = navigateToAdmin
                                )
                            }.addOnFailureListener { exception ->
                                onFailure(exception.message)
                            }
                        }
                        .addOnFailureListener { exception ->
                            onFailure(exception.message)
                        }
                } else {
                    onFailure("User ID is null")
                }
            } else {
                onFailure(task.exception?.message)
            }
        }
}

fun saveUserToDatabase(
    userId: String,
    email: String,
    imageUrl: String,
    onFailure: (String?) -> Unit,
    voterID: String,
    phoneNumber: String,
    curp: String,
    lastName: String,
    firstName: String,
    navigateToAdmin: () -> Unit,
) {
    val userMap = mapOf(
        "firstName" to firstName,
        "lastName" to lastName,
        "email" to email,
        "curp" to curp,
        "phoneNumber" to phoneNumber,
        "voterID" to voterID,
        "imageUrl" to imageUrl
    )

    val usersRef = FirebaseDatabase.getInstance().getReference("Users")
    usersRef.child(userId).setValue(userMap)
        .addOnSuccessListener {
            navigateToAdmin()
            Log.i("angel", "User create sucesfully")
        }
        .addOnFailureListener { exception ->
            onFailure(exception.message)
        }
}