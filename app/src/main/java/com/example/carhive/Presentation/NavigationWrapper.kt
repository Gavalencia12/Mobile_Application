package com.example.carhive.Presentation


import ThirdRegisterScreen
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.carhive.CarScreen
import com.example.carhive.Presentation.initial.Login.view.LoginScreen
import com.example.carhive.Presentation.initial.Register.view.FirstRegisterScreen
import com.example.carhive.Presentation.initial.Register.view.SecondRegisterScreen
import com.example.carhive.Presentation.user.view.UserScreen
import com.example.carhive.Presentation.user.view.sellerScreen

@Composable
fun NavigationWrapper(
    navHostController: NavHostController,
    authViewModel: AuthViewModel = hiltViewModel() // Inyectar el ViewModel
) {
    // Obteniendo los estados del ViewModel
    val isAuthenticated by authViewModel.isAuthenticated.collectAsState()
    val userRole by authViewModel.userRole.collectAsState()

    // Determinamos la startDestination con base en la autenticación y rol
    val startDestination = when {
        isAuthenticated && userRole == 0 -> "Admin"   // Rol de Admin
        isAuthenticated && userRole == 1 -> "Seller"  // Rol de Seller
        isAuthenticated && userRole == 2 -> "User"    // Rol de User
        else -> "Login"                               // No autenticado
    }

    // Configuramos el NavHost
    NavHost(navController = navHostController, startDestination = startDestination) {
        composable("Login") {
            LoginScreen(
                navHostController = navHostController,
                navigateToRegister = { navHostController.navigate("FirstRegister") }
            )
        }
        composable("FirstRegister") {
            FirstRegisterScreen(
                navigateToLogin = { navHostController.navigate("Login") },
                navigateToNext = { navHostController.navigate("SecondRegister") }
            )
        }
        composable("SecondRegister") {
            SecondRegisterScreen(
                navigateToPrevious = { navHostController.navigate("FirstRegister") },
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
            UserScreen(
                navigateToLogin = { navHostController.navigate("Login")}
            )
        }
        composable("Seller") {
            sellerScreen(
                navigateToLogin = { navHostController.navigate("Login")}
            )
        }
        composable("Admin") {
            CarScreen()
        }
    }
}
