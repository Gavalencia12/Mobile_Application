package com.example.carhive.CAR

import CarList
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.carhive.CarScreen
import com.example.carhive.ui.theme.CarHiveTheme

class CarActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CarHiveTheme {
                Column {
                    CarScreen<Any>()
                    Spacer(modifier = Modifier.height(16.dp))
                    CarList()
                }
            }
        }
    }
}
