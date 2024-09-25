package com.example.carhive.Presentation.admin.items

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Brush
import coil.compose.rememberAsyncImagePainter
import com.example.carhive.CarViewModel
import com.example.carhive.Data.admin.models.Car

@Composable
fun CarList(viewModel: CarViewModel) {
    val cars by viewModel.carList.collectAsState()
    var selectedCar by remember { mutableStateOf<Car?>(null) }

    LazyColumn(modifier = Modifier.padding(16.dp)) {
        items(cars) { car ->
            Row(modifier = Modifier.fillMaxWidth()) {
                if (car.imageUrls.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clickable { selectedCar = car },
                        contentAlignment = Alignment.Center // Alineamos el contenido al centro
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(car.imageUrls.first()),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )

                        // Añadimos el fondo semitransparente al número
                        Text(
                            text = "${car.imageUrls.size}", // Contador de imágenes
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .background(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(Color.Black.copy(alpha = 0.6f), Color.Transparent)
                                    )
                                )
                                .padding(8.dp)
                        )
                    }
                } else {
                    Text(text = "No images available")
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(text = car.name)

                    Button(onClick = { viewModel.updateCar(car, "New Car Name") }) {
                        Text("Update")
                    }
                    Button(onClick = { viewModel.deleteCar(car) }) {
                        Text("Delete")
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    selectedCar?.let { car ->
        CarImageAlbumDialog(car = car, onDismiss = { selectedCar = null })
    }
}
