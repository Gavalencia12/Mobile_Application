package com.example.carhive.Presentation.seller.items

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.rememberAsyncImagePainter
import com.example.carhive.Data.admin.models.Car
import com.example.carhive.R

@Composable
fun CarImageAlbumDialog(car: Car, onDismiss: () -> Unit) {
    // State to keep track of the selected image for full-screen view
    var selectedImageUrl by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Surface {
            Column(modifier = Modifier.padding(16.dp)) {
                // Display car name
                Text(text = car.modelo)
                Spacer(modifier = Modifier.height(16.dp))

                // Display total number of images
                Text(text = "Images: ${car.imageUrls.size}")

                Spacer(modifier = Modifier.height(16.dp))

                // Image carousel
                LazyRow {
                    items(car.imageUrls) { imageUrl ->
                        Image(
                            painter = rememberAsyncImagePainter(imageUrl),
                            contentDescription = null,
                            modifier = Modifier
                                .size(200.dp)
                                .padding(end = 8.dp)
                                .clickable { selectedImageUrl = imageUrl }  // Set selected image on click
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Button to close the dialog
                Button(onClick = onDismiss) {
                    Text(text = stringResource(id = R.string.close))
                }
            }
        }
    }

    // Show full-screen dialog if an image is selected
    selectedImageUrl?.let { imageUrl ->
        FullScreenImageDialog(imageUrl = imageUrl, onDismiss = { selectedImageUrl = null })
    }
}

@Composable
fun FullScreenImageDialog(imageUrl: String, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Full-screen image
                Image(
                    painter = rememberAsyncImagePainter(imageUrl),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable { onDismiss() }  // Close dialog on click
                )
            }
        }
    }
}