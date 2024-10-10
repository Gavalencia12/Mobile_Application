package com.example.carhive.Presentation.seller.items

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.carhive.Presentation.seller.viewModel.CarViewModel
import com.example.carhive.Data.admin.models.Car
import com.example.carhive.R

@Composable
fun CarList(viewModel: CarViewModel) {
    // Collect the list of cars from the ViewModel
    val cars by viewModel.carList.collectAsState()

    // State variables for managing dialogs and selected car
    var selectedCar by remember { mutableStateOf<Car?>(null) }
    var showUpdateDialog by remember { mutableStateOf(false) }
    var showImageAlbumDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmationDialog by remember { mutableStateOf(false) }

    // Main list of cars
    LazyColumn(modifier = Modifier.padding(16.dp)) {
        items(cars) { car ->
            Row(modifier = Modifier.fillMaxWidth()) {
                // Display car image or "No images available" text
                if (car.imageUrls.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clickable {
                                selectedCar = car
                                showImageAlbumDialog = true
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        // Display the first image of the car
                        Image(
                            painter = rememberAsyncImagePainter(car.imageUrls.first()),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )

                        // Overlay text showing the number of images
                        Text(
                            text = "${car.imageUrls.size}",
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .background(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Black.copy(alpha = 0.6f),
                                            Color.Transparent
                                        )
                                    )
                                )
                                .padding(8.dp)
                        )
                    }
                } else {
                    Text(text = stringResource(id = R.string.no_images_available))
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Car name and action buttons
                Column {
                    Text(text = car.modelo)

                    // Update button
                    Button(onClick = {
                        selectedCar = car
                        showUpdateDialog = true
                    }) {
                        Text(stringResource(id = R.string.update))
                    }

                    // Delete button
                    Button(onClick = {
                        selectedCar = car
                        showDeleteConfirmationDialog = true
                    }) {
                        Text(stringResource(id = R.string.delete))
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Update dialog
    if (showUpdateDialog && selectedCar != null) {
        UpdateCarDialog(
            car = selectedCar!!,
            onDismiss = {
                showUpdateDialog = false
                selectedCar = null
            },
            onUpdate = { modelo, carColor, carSpeed, carAddOn, carDescription, carPrice,  imageUris ->
                viewModel.updateCar(selectedCar!!, modelo, carColor, carSpeed, carAddOn, carDescription,carPrice, imageUris)
                showUpdateDialog = false
            }
        )
    }

    // Image album dialog
    if (showImageAlbumDialog && selectedCar != null) {
        CarImageAlbumDialog(
            car = selectedCar!!,
            onDismiss = {
                showImageAlbumDialog = false
                selectedCar = null
            }
        )
    }

    // Delete confirmation dialog
    if (showDeleteConfirmationDialog && selectedCar != null) {
        AlertDialog(
            onDismissRequest = {
                showDeleteConfirmationDialog = false
                selectedCar = null
            },
            title = { Text(stringResource(id = R.string.confirm_deletion)) },
            text = { Text(stringResource(id = R.string.deletion_confirmation_message)) },
            confirmButton = {
                Button(onClick = {
                    viewModel.deleteCar(selectedCar!!) // Este método eliminará el carro y sus imágenes
                    showDeleteConfirmationDialog = false
                    selectedCar = null
                }) {
                    Text(stringResource(R.string.yes))
                }
            },
            dismissButton = {
                Button(onClick = {
                    showDeleteConfirmationDialog = false
                    selectedCar = null
                }) {
                    Text(stringResource(R.string.no))
                }
            }
        )
    }

}