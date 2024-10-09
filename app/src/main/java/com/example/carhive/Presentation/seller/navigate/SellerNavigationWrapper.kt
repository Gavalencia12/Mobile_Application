package com.example.carhive.Presentation.seller.navigate

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.carhive.CarScreen
import com.example.carhive.Presentation.seller.view.HomeScreen

@Composable
fun SellerNavigationWrapper(
    sellerNavHostController: NavHostController,
    parentNavHostController: NavHostController
) {
    NavHost(navController = sellerNavHostController, startDestination = "Home") {
        composable("Home") {
            HomeScreen(
                navigateToLogin = { parentNavHostController.navigate("Login") },
                navigateToCrud = { sellerNavHostController.navigate("Crud")}
            )
        }
        // Aquí puedes agregar más pantallas específicas para el rol de Seller
        composable("Crud") {
            CarScreen()
        }
    }
}