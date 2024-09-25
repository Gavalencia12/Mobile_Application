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
import com.example.carhive.Presentation.admin.items.CarForm
import com.example.carhive.Presentation.admin.items.CarList
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarScreen(viewModel: CarViewModel = androidx.lifecycle.viewmodel.compose.viewModel()) {
    var isModalOpen by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("CarHive") },
                actions = {
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
        // Car list and form modal
        Box(modifier = Modifier.padding(paddingValues)) {
            CarList(viewModel = viewModel)  // Show car list

            if (isModalOpen) {
                CarForm(
                    viewModel = viewModel,
                    onDismiss = { isModalOpen = false }
                )
            }
        }
    }
}