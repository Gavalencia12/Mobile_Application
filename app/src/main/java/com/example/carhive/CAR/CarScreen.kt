package com.example.carhive

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter

@Composable
fun <T> CarScreen(viewModel: CarViewModel = viewModel()) {
    var carName by remember { mutableStateOf("") }
    val imageUris by viewModel.imageUris.collectAsState()
    val context = LocalContext.current

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            if (imageUris.size < 3) {
                viewModel.addImageUri(it)
            } else {
                Toast.makeText(context, "Only 3 images are allowed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Column(modifier = Modifier.padding(16.dp)) {
        TextField(
            value = carName,
            onValueChange = { carName = it },
            label = { Text("Car Name") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = { imagePickerLauncher.launch("image/*") }, modifier = Modifier.fillMaxWidth()) {
            Text("Select Image")
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn {
            items(imageUris) { uri ->
                Row(modifier = Modifier.fillMaxWidth()) {
                    Image(
                        painter = rememberAsyncImagePainter(uri),
                        contentDescription = null,
                        modifier = Modifier.size(100.dp),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = { viewModel.removeImageUri(uri) }) {
                        Text("Remove")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (imageUris.size == 3) {
                    viewModel.createCar(carName)
                    carName = ""
                } else {
                    Toast.makeText(context, "You need to select 3 images", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Create Car")
        }
    }
}
