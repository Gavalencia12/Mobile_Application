package com.example.carhive.Presentation.user.navigate

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.carhive.Presentation.user.view.UserScreen

@Composable
fun UserNavigationWrapper(
    userNavHostController: NavHostController,
    parentNavHostController: NavHostController
) {
    NavHost(navController = userNavHostController, startDestination = "Home") {
        composable("Home") {
            UserScreen(
//                navigateToDetails = { userNavHostController.navigate("SellerDetails") },
                navigateToLogin = { parentNavHostController.navigate("Login")}
            )
        }

    }
}