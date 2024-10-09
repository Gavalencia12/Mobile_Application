package com.example.carhive

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.carhive.Presentation.seller.items.CarForm
import com.example.carhive.Presentation.seller.items.CarList
import com.example.carhive.Presentation.seller.viewModel.CarViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarScreen(viewModel: CarViewModel = androidx.lifecycle.viewmodel.compose.viewModel()) {
    // State to control the visibility of the car form modal
    var isModalOpen by remember { mutableStateOf(false) }
    // Coroutine scope for launching asynchronous operations
    val coroutineScope = rememberCoroutineScope()

    // Scaffold provides the basic material design visual layout structure
    Scaffold(
        topBar = {
            // Top app bar with title and add button
            TopAppBar(
                title = { Text("CarHive") },
                actions = {
                    // Add button to open the car form
                    IconButton(onClick = {
                        coroutineScope.launch {
                            isModalOpen = true
                        }
                    }) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Add Car")
                    }
                }
            )
        }
    ) { paddingValues ->
        // Content area of the screen
        Box(modifier = Modifier.padding(paddingValues)) {
            // Display the list of cars
            CarList(viewModel = viewModel)

            // Show the car form modal if isModalOpen is true
            if (isModalOpen) {
                CarForm(
                    viewModel = viewModel,
                    onDismiss = { isModalOpen = false }
                )
            }
        }
    }
}