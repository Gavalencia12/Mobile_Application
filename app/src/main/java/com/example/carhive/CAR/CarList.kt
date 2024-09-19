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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.carhive.CarViewModel
import com.example.carhive.models.Car

@Composable
fun CarList(viewModel: CarViewModel = viewModel()) {
    val cars by viewModel.carList.collectAsState()
    var selectedCar by remember { mutableStateOf<Car?>(null) } // Estado para manejar el carro seleccionado

    LazyColumn(modifier = Modifier.padding(16.dp)) {
        items(cars) { car ->
            Row(modifier = Modifier.fillMaxWidth()) {
                // Mostrar la primera imagen del carro, si tiene imágenes
                if (car.imageUrls.isNotEmpty()) {
                    Box(modifier = Modifier.size(100.dp)) {
                        // Imagen como botón
                        Image(
                            painter = rememberAsyncImagePainter(car.imageUrls.first()),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxSize()
                                .clickable { selectedCar = car }, // Imagen es clickeable
                            contentScale = ContentScale.Crop
                        )

                        // Recuadro centrado que muestra cuántas imágenes tiene el carro
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .fillMaxSize()
                                .alpha(0.7f) // Un poco de transparencia para mejor visibilidad
                        ) {
                            Text(
                                text = "${car.imageUrls.size} Photos",
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
                    Text(text = car.name)

                    // Botón para actualizar
                    Button(onClick = {
                        viewModel.updateCar(car, "New Name")  // Cambia el nombre con el valor deseado
                    }) {
                        Text("Update")
                    }

                    // Botón para eliminar
                    Button(onClick = {
                        viewModel.deleteCar(car)
                    }) {
                        Text("Delete")
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Mostrar el Dialog si se selecciona un carro
    selectedCar?.let { car ->
        CarImageAlbumDialog(car = car, onDismiss = { selectedCar = null })
    }
}

@Composable
fun CarImageAlbumDialog(car: Car, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Text(text = car.name, style = MaterialTheme.typography.headlineLarge)

            Spacer(modifier = Modifier.height(16.dp))

            LazyRow(modifier = Modifier.fillMaxWidth()) {
                items(car.imageUrls) { imageUrl ->
                    Image(
                        painter = rememberAsyncImagePainter(imageUrl),
                        contentDescription = null,
                        modifier = Modifier.size(200.dp).padding(8.dp),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) {
                Text("Close")
            }
        }
    }
}
