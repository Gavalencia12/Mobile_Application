package com.example.carhive

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter


@Composable
fun CarList(viewModel: CarViewModel = viewModel()) {
    val cars by viewModel.carList.collectAsState()

    LazyColumn(modifier = Modifier.padding(16.dp)) {
        items(cars) { car ->
            Row(modifier = Modifier.fillMaxWidth()) {
                // Mostrar la primera imagen del carro, si tiene imágenes
                if (car.imageUrls.isNotEmpty()) {
                    Image(
                        painter = rememberAsyncImagePainter(car.imageUrls.first()),  // Cargar la primera imagen desde la URL
                        contentDescription = null,
                        modifier = Modifier.size(100.dp),
                        contentScale = ContentScale.Crop
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(text = car.name)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
