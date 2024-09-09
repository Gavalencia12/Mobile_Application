package com.example.carhive

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel // Importa el método viewModel de Compose
import com.example.carhive.models.Car
import com.example.carhive.ui.theme.CarHiveTheme

class CarActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CarHiveTheme {
                CarScreen()
            }
        }
    }
}

@Composable
fun CarScreen(viewModel: CarViewModel = viewModel()) {
    var carName by remember { mutableStateOf("") }
    val carList by viewModel.carList.collectAsState()

    Column(modifier = Modifier.padding(16.dp)) {
        TextField(
            value = carName,
            onValueChange = { carName = it },
            label = { Text("Car Name") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Text)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                if (viewModel.carIdToUpdate == null) {
                    viewModel.createCar(carName)
                } else {
                    viewModel.updateCar(carName)
                }
                carName = ""
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = if (viewModel.carIdToUpdate == null) "Create Car" else "Update Car :)")
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {
            items(carList) { car ->
                CarItem(car = car, onUpdateClick = {
                    viewModel.prepareForUpdate(car)
                    carName = car.name
                }, onDeleteClick = {
                    viewModel.deleteCar(car)
                })
            }
        }
    }
}

@Composable
fun CarItem(car: Car, onUpdateClick: (Car) -> Unit, onDeleteClick: (Car) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = car.name)
        Row {
            Button(onClick = { onUpdateClick(car) }) {
                Text("Update")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = { onDeleteClick(car) }) {
                Text("Delete")
            }
        }
    }
}
