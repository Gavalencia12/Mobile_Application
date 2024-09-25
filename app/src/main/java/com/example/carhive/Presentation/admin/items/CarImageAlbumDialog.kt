package com.example.carhive.Presentation.admin.items

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.rememberAsyncImagePainter
import com.example.carhive.Data.admin.models.Car

@Composable
fun CarImageAlbumDialog(car: Car, onDismiss: () -> Unit) {
    var selectedImageUrl by remember { mutableStateOf<String?>(null) } // Estado para la imagen seleccionada

    Dialog(onDismissRequest = onDismiss) {
        Surface {
            Column(modifier = Modifier.padding(16.dp)) {
                // Nombre del carro
                Text(text = car.name)
                Spacer(modifier = Modifier.height(16.dp))

                // Mostrar cuántas imágenes hay en total
                Text(text = "Images: ${car.imageUrls.size}")

                Spacer(modifier = Modifier.height(16.dp))

                // Carrusel de imágenes
                LazyRow {
                    items(car.imageUrls) { imageUrl ->
                        Image(
                            painter = rememberAsyncImagePainter(imageUrl),
                            contentDescription = null,
                            modifier = Modifier
                                .size(200.dp)
                                .padding(end = 8.dp)
                                .clickable { selectedImageUrl = imageUrl }  // Al hacer clic, establece la imagen seleccionada
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Botón para cerrar el diálogo
                Button(onClick = onDismiss) {
                    Text("Close")
                }
            }
        }
    }

    // Si hay una imagen seleccionada, mostrarla en un diálogo de pantalla completa
    selectedImageUrl?.let { imageUrl ->
        FullScreenImageDialog(imageUrl = imageUrl, onDismiss = { selectedImageUrl = null })
    }
}

@Composable
fun FullScreenImageDialog(imageUrl: String, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Imagen en pantalla completa
                Image(
                    painter = rememberAsyncImagePainter(imageUrl),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable { onDismiss() }  // Al hacer clic, cerrar el diálogo
                )
            }
        }
    }
}
