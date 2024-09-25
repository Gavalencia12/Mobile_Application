package com.example.carhive.Presentation.admin.items

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
import com.example.carhive.CarViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarForm(viewModel: CarViewModel, onDismiss: () -> Unit) {
    var carName by remember { mutableStateOf("") }
    val imageUris by viewModel.imageUris.collectAsState()

    // Registrar un lanzador de actividad para seleccionar múltiples imágenes
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents(),
        onResult = { uris: List<Uri> ->
            // Añadir las imágenes seleccionadas al ViewModel
            viewModel.addImageUris(uris)
        }
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = Modifier.padding(bottom = 32.dp) // Agregar padding en la parte inferior
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Campo de texto para el nombre del carro
            TextField(
                value = carName,
                onValueChange = { carName = it },
                label = { Text("Car Name") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Mostrar cuántas imágenes han sido seleccionadas
            Text(
                text = "Images selected: ${imageUris.size}/5",
                fontSize = 14.sp,
                modifier = Modifier.align(androidx.compose.ui.Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Mostrar las imágenes seleccionadas
            LazyRow {
                items(imageUris) { uri ->
                    Row {
                        Image(
                            painter = rememberAsyncImagePainter(uri),
                            contentDescription = null,
                            modifier = Modifier.size(100.dp) // Ajustar tamaño de las imágenes seleccionadas
                        )
                        // Botón para eliminar una imagen
                        IconButton(onClick = { viewModel.removeImageUri(uri) }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Remove Image")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Botón para seleccionar imágenes
            Button(
                onClick = {
                    if (imageUris.size < 5) {
                        imagePickerLauncher.launch("image/*")  // Permitir seleccionar imágenes múltiples
                    }
                },
                enabled = imageUris.size < 5, // Solo habilitado si hay menos de 5 imágenes seleccionadas
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Select Images")
            }

            Spacer(modifier = Modifier.height(24.dp))  // Aumentar el espacio aquí para más separación

            // Botón para crear el carro, movido más arriba para evitar la superposición con los botones del sistema
            Button(
                onClick = {
                    viewModel.createCar(carName)
                    carName = ""
                    onDismiss()
                },
                enabled = imageUris.size == 5,  // Solo habilitado si hay 5 imágenes seleccionadas
                modifier = Modifier.fillMaxWidth() // Hacer que el botón ocupe todo el ancho
            ) {
                Text("Create Car")
            }

            Spacer(modifier = Modifier.height(32.dp))  // Espacio extra para evitar superposición con los botones del sistema
        }
    }
}
