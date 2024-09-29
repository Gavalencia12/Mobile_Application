package com.example.carhive.Presentation

import ThirdRegisterScreen
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.carhive.Presentation.initial.Login.view.LoginScreen
import com.example.carhive.Presentation.initial.Register.view.SecondRegisterScreen
import com.example.carhive.Presentation.initial.Register.view.FirstRegisterScreen
import com.example.carhive.Presentation.user.UserScreen

@Composable
fun NavigationWrapper(
    navHostController: NavHostController,
) {
    NavHost(navController = navHostController, startDestination = "Login") {
        composable("Login") {
            LoginScreen(
                navigateToUser = { navHostController.navigate("User") },
                navigateToRegister = { navHostController.navigate("Register") }
            )
        }
        composable("Register") {
            FirstRegisterScreen(
                navigateToLogin = { navHostController.navigate("Login") },
                navigateToNext = { navHostController.navigate("SecondRegister") }
            )
        }
        composable("SecondRegister") {
            SecondRegisterScreen(
                navigateToPrevious = { navHostController.navigate("SecondRegister") },
                navigateToNext = { navHostController.navigate("ThirdRegister") }
            )
        }
        composable("ThirdRegister") {
            ThirdRegisterScreen(
                navigateToPrevious = { navHostController.navigate("SecondRegister") },
                navigateToUser = { navHostController.navigate("User") }
            )
        }
        composable("User") {
            UserScreen()
        }

    }
}