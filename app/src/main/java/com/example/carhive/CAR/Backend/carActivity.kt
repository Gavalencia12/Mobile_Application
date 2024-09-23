package com.example.carhive.CAR.Backend

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.carhive.CarScreen
import com.example.carhive.ui.CarList
import com.example.carhive.ui.theme.CarHiveTheme
import com.example.carhive.CarViewModel

class CarActivity : ComponentActivity() {

    // Inicializa el ViewModel utilizando viewModels()
    private val carViewModel: CarViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CarHiveTheme {
                Column {

                    CarScreen(viewModel = carViewModel)
                    Spacer(modifier = Modifier.height(16.dp))
                    CarList(viewModel = carViewModel)
                }
            }
        }
    }
}
