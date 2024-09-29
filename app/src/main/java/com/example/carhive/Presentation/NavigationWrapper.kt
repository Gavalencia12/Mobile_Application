package com.example.carhive.Presentation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.carhive.CarScreen
import com.example.carhive.Presentation.initial.Login.LoginScreen
import com.example.carhive.Presentation.initial.Register.SecondRegisterScreen
import com.example.carhive.Presentation.initial.Register.SignupScreen
import com.example.carhive.Presentation.initial.Register.ThirdSignupScreen
import com.google.firebase.auth.FirebaseAuth

@Composable
fun NavigationWrapper(
    navHostController: NavHostController,
    auth: FirebaseAuth,
) {
    NavHost(navController = navHostController, startDestination = "Admin") {
        composable("Login") {
            LoginScreen(
                auth,
                navigateToHome = { navHostController.navigate("Admin") },
                navigateToRegister = { navHostController.navigate("Register") }
            )
        }
        composable("Register") {
            SignupScreen(
                auth,
                navigateToSecondRegister = { firstName, lastName, email, password ->
                    navHostController.currentBackStackEntry?.savedStateHandle?.set(
                        "fistsName", firstName
                    )
                    navHostController.currentBackStackEntry?.savedStateHandle?.set(
                        "lastName", lastName
                    )
                    navHostController.currentBackStackEntry?.savedStateHandle?.set(
                        "email", email
                    )
                    navHostController.currentBackStackEntry?.savedStateHandle?.set(
                        "password", password
                    )
                    navHostController.navigate("SecondRegister")
                }
            )
        }
        composable("SecondRegister"){
            val firstName = navHostController.previousBackStackEntry?.savedStateHandle?.get<String>("firstName")?:""
            val lastName = navHostController.previousBackStackEntry?.savedStateHandle?.get<String>("lastName")?:""
            val email = navHostController.previousBackStackEntry?.savedStateHandle?.get<String>("email")?:""
            val password = navHostController.previousBackStackEntry?.savedStateHandle?.get<String>("password")?:""

            SecondRegisterScreen(
                firstName = firstName,
                lastName = lastName,
                email = email,
                password = password,
                auth = auth,
                navigateToThirdRegister = { firstName, lastName, email, password, curp, voterID, phoneNumber ->
                    navHostController.currentBackStackEntry?.savedStateHandle?.set(
                        "firstname", firstName
                    )
                    navHostController.currentBackStackEntry?.savedStateHandle?.set(
                        "lastName", lastName
                    )
                    navHostController.currentBackStackEntry?.savedStateHandle?.set(
                        "email", email
                    )
                    navHostController.currentBackStackEntry?.savedStateHandle?.set(
                        "password", password
                    )
                    navHostController.currentBackStackEntry?.savedStateHandle?.set(
                        "curp", curp
                    )
                    navHostController.currentBackStackEntry?.savedStateHandle?.set(
                        "voterID", voterID
                    )
                    navHostController.currentBackStackEntry?.savedStateHandle?.set(
                        "phoneNumber", phoneNumber
                    )
                    navHostController.navigate("ThirdRegister")
                }
            )
        }
        composable("ThirdRegister"){
            val firstName = navHostController.previousBackStackEntry?.savedStateHandle?.get<String>("firstName")?:""
            val lastName = navHostController.previousBackStackEntry?.savedStateHandle?.get<String>("lastName")?:""
            val email = navHostController.previousBackStackEntry?.savedStateHandle?.get<String>("email")?:""
            val password = navHostController.previousBackStackEntry?.savedStateHandle?.get<String>("password")?:""

            val curp = navHostController.previousBackStackEntry?.savedStateHandle?.get<String>("curp")?:""
            val voterID = navHostController.previousBackStackEntry?.savedStateHandle?.get<String>("voterID")?:""
            val phoneNumber = navHostController.previousBackStackEntry?.savedStateHandle?.get<String>("phoneNumber")?:""

            ThirdSignupScreen(
                auth = auth,
                firstName = firstName,
                lastName = lastName,
                email = email,
                password = password,

                curp = curp,
                voterID = voterID,
                phoneNumber = phoneNumber,
                navigateToAdmin = { navHostController.navigate("Admin")}

            )
        }
        composable("Admin") {
            CarScreen()
        }

    }
}