package com.example.carhive.Presentation.seller.items

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.carhive.Presentation.seller.viewModel.CarViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarForm(viewModel: CarViewModel, onDismiss: () -> Unit) {
    // State for car name input
    var carModelo by remember { mutableStateOf("") }
    var carColor by remember { mutableStateOf("") }
    var carSpeed by remember { mutableStateOf("") }
    var carAddOn by remember { mutableStateOf("") }
    var carDescription by remember { mutableStateOf("") }
    var carPrice by remember { mutableStateOf("") }


    // Collect image URIs from ViewModel
    val imageUris by viewModel.imageUris.collectAsState()

    // Activity launcher for selecting multiple images
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents(),
        onResult = { uris: List<Uri> ->
            // Add selected images to ViewModel
            viewModel.addImageUris(uris)
        }
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = Modifier.padding(bottom = 32.dp) // Add padding at the bottom
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Text field for car name input
            TextField(
                value = carModelo,
                onValueChange = { carModelo= it },
                label = { Text("Modelo") },
                modifier = Modifier.fillMaxWidth()
            )

            TextField(
                value = carColor,
                onValueChange = { carColor = it },
                label = { Text("Color") },
                modifier = Modifier.fillMaxWidth()
            )

            TextField(
                value = carSpeed,
                onValueChange = { carSpeed = it },
                label = { Text("Speed") },
                modifier = Modifier.fillMaxWidth()
            )

            TextField(
                value = carAddOn,
                onValueChange = { carAddOn = it },
                label = { Text("Add on") },
                modifier = Modifier.fillMaxWidth()
            )

            TextField(
                value = carDescription,
                onValueChange = { carDescription= it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth()
            )
            TextField(
                value = carPrice,
                onValueChange = { carPrice = it },
                label = { Text("Price") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Display number of selected images
            Text(
                text = "Images selected: ${imageUris.size}/5",
                fontSize = 14.sp,
                modifier = Modifier.align(androidx.compose.ui.Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Display selected images
            LazyRow {
                items(imageUris) { uri ->
                    Row {
                        Image(
                            painter = rememberAsyncImagePainter(uri),
                            contentDescription = null,
                            modifier = Modifier.size(100.dp) // Adjust size of selected images
                        )
                        // Button to remove an image
                        IconButton(onClick = { viewModel.removeImageUri(uri) }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Remove Image")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Button to select images
            Button(
                onClick = {
                    if (imageUris.size < 5) {
                        imagePickerLauncher.launch("image/*")  // Allow selecting multiple images
                    }
                },
                enabled = imageUris.size < 5, // Only enabled if less than 5 images are selected
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Select Images")
            }

            Spacer(modifier = Modifier.height(24.dp))  // Increase space here for more separation

            // Button to create the car, moved up to avoid overlap with system buttons
            Button(
                onClick = {
                    viewModel.createCar(carModelo, carColor, carSpeed, carAddOn, carDescription, carPrice)
                    carModelo = ""
                    carColor = ""
                    carSpeed = ""
                    carAddOn = ""
                    carDescription = ""
                    carPrice = ""
                    onDismiss()
                },
                enabled = imageUris.size == 5,  // Only enabled if exactly 5 images are selected
                modifier = Modifier.fillMaxWidth() // Make the button occupy full width
            ) {
                Text("Create Car")
            }

            Spacer(modifier = Modifier.height(32.dp))  // Extra space to avoid overlap with system buttons
        }
    }
}