import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.carhive.CarViewModel
import com.example.carhive.R
import com.example.carhive.models.Car

/**
 * Displays a list of cars with options to update or delete each car.
 * A dialog shows the car's image album when an image is clicked.
 */
@Composable
fun CarList(viewModel: CarViewModel = viewModel()) {
    val cars by viewModel.carList.collectAsState()
    var selectedCar by remember { mutableStateOf<Car?>(null) }
    val newName = stringResource(R.string.new_name)

    LazyColumn(modifier = Modifier.padding(16.dp)) {
        items(cars) { car ->
            Row(modifier = Modifier.fillMaxWidth()) {
                // Display the first image of the car if available
                if (car.imageUrls.isNotEmpty()) {
                    Box(modifier = Modifier.size(100.dp)) {
                        Image(
                            painter = rememberAsyncImagePainter(car.imageUrls.first()),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxSize()
                                .clickable { selectedCar = car },
                            contentScale = ContentScale.Crop
                        )
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .fillMaxSize()
                                .alpha(0.7f)
                        ) {
                            Text(
                                text = stringResource(R.string.num_photos, car.imageUrls.size),
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontSize = 16.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .background(MaterialTheme.colorScheme.primary)
                                    .padding(4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    // Display car name
                    Text(text = car.name)

                    // Button to update the car
                    Button(onClick = {
                        viewModel.updateCar(car, newName)
                    }) {
                        Text(stringResource(R.string.update))
                    }

                    // Button to delete the car
                    Button(onClick = {
                        viewModel.deleteCar(car)
                    }) {
                        Text(stringResource(R.string.delete))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Show the image album dialog when a car is selected
    selectedCar?.let { car ->
        CarImageAlbumDialog(car = car, onDismiss = { selectedCar = null })
    }
}

/**
 * Dialog to display a car's image album.
 *
 * @param car The selected car.
 * @param onDismiss Function to close the dialog.
 */
@Composable
fun CarImageAlbumDialog(car: Car, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.background,
            modifier = Modifier.wrapContentSize()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = car.name, style = MaterialTheme.typography.headlineLarge)

                Spacer(modifier = Modifier.height(16.dp))

                // Display all car images in a horizontal scrollable list
                LazyRow(modifier = Modifier.fillMaxWidth()) {
                    items(car.imageUrls) { imageUrl ->
                        Image(
                            painter = rememberAsyncImagePainter(imageUrl),
                            contentDescription = null,
                            modifier = Modifier
                                .size(200.dp)
                                .padding(8.dp),
                            contentScale = ContentScale.Crop
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) {
                    Text(stringResource(R.string.close))
                }
            }
        }
    }
}
