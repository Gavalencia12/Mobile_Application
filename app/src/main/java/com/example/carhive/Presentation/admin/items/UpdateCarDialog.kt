package com.example.carhive.Presentation.admin.items

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.carhive.Data.admin.models.Car


@Composable
fun UpdateCarDialog(
    car: Car,
    onDismiss: () -> Unit,
    onUpdate: (String, String, String, String, String, String, List<Uri>) -> Unit
) {
    // State variables
    var carModelo by remember { mutableStateOf(car.modelo) }
    var carColor by remember { mutableStateOf(car.color) }
    var carSpeed by remember { mutableStateOf(car.speed) }
    var carAddOn by remember { mutableStateOf(car.addOn) }
    var carPrice by remember { mutableStateOf(car.price) }
    var carDescription by remember { mutableStateOf(car.description) }
    var imageUris by remember { mutableStateOf(car.imageUrls.map { Uri.parse(it) }) }
    var fullScreenImageUrl by remember { mutableStateOf<String?>(null) }
    var imageCount by remember { mutableStateOf(imageUris.size) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents(),
        onResult = { uris ->
            val filteredUris = uris.filter { uri -> !imageUris.contains(uri) }
            if (imageUris.size + filteredUris.size <= 5) {
                imageUris = imageUris.toMutableList().apply { addAll(filteredUris) }
                imageCount = imageUris.size
            } else {
                // Show message if limit exceeded
                println("Maximum of 5 images allowed")
            }
        }
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Update Car") },
        text = {
            Column {
                TextField(value = carModelo, onValueChange = { carModelo = it }, label = { Text("Car Modelo") })
                TextField(value = carColor, onValueChange = { carColor = it }, label = { Text("Car Color") })
                TextField(value = carSpeed, onValueChange = { carSpeed = it }, label = { Text("Car Speed") })
                TextField(value = carAddOn, onValueChange = { carAddOn = it }, label = { Text("Car Add On") })
                TextField(value = carDescription, onValueChange = { carDescription = it }, label = { Text("Car Description") })
                TextField(value = carPrice, onValueChange = { carPrice = it }, label = { Text("Car Price") })

                Spacer(modifier = Modifier.height(16.dp))

                // Image count and thumbnails
                Text("Images ($imageCount/5):")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    imageUris.forEach { uri ->
                        Box(modifier = Modifier.size(100.dp)) {
                            Image(painter = rememberAsyncImagePainter(uri), contentDescription = null, modifier = Modifier
                                .fillMaxSize()
                                .clickable { fullScreenImageUrl = uri.toString() })
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete Image",
                                tint = Color.White,
                                modifier = Modifier
                                    .size(24.dp)
                                    .align(Alignment.TopEnd)
                                    .background(Color.Red, shape = CircleShape)
                                    .padding(4.dp)
                                    .clickable {
                                        imageUris = imageUris.toMutableList().apply { remove(uri) }
                                        imageCount = imageUris.size
                                    }
                            )
                        }
                    }
                }

                // Button to add more images
                if (imageUris.size < 5) {
                    Button(onClick = { imagePickerLauncher.launch("image/*") }) {
                        Text("Add Images")
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onUpdate(carModelo, carColor, carSpeed, carAddOn, carDescription, carPrice, imageUris)
                    onDismiss()
                },
                enabled = imageUris.size <= 5
            ) {
                Text("Update")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )

    // Full-screen image view
    fullScreenImageUrl?.let { url ->
        FullScreenImageDialog(imageUrl = url, onDismiss = { fullScreenImageUrl = null })
    }

}