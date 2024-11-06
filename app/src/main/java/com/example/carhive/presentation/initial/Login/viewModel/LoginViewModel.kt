package com.example.carhive.presentation.initial.Login.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firebaseDatabase: FirebaseDatabase // Suponiendo que usas Realtime Database o Firestore
) : ViewModel() {

    private val _isLogin = MutableStateFlow(false)
    val isLogin: StateFlow<Boolean> get() = _isLogin

    private val _loginError = MutableStateFlow<String?>(null)
    val loginError: StateFlow<String?> get() = _loginError

    // Función para manejar el clic en login y decidir a dónde navegar
    fun onLoginClick(email: String, password: String, navigateBasedOnRole: (String) -> Unit, navigateToVerifyEmail: () -> Unit) {
        // Verificar si el email y la contraseña están vacíos
        if (email.isBlank() || password.isBlank()) {
            _loginError.value = "Email and password cannot be empty."
            return
        }

        viewModelScope.launch {
            firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val userId = firebaseAuth.currentUser?.uid

                        userId?.let {
                            // Aquí verificas si el usuario está baneado
                            checkIfUserIsBanned(it) { banned ->
                                if (banned) {
                                    _loginError.value = "Your account has been banned."
                                } else {
                                    // Continuar con la lógica de navegación basada en el rol
                                    getUserRole(it, navigateBasedOnRole)
                                }
                            }
                        }
                    } else {
                        val exception = task.exception
                        if (exception?.message == "Email not verified.") {
                            navigateToVerifyEmail()
                        } else {
                            _loginError.value = "Incorrect credentials."
                        }
                    }
                }
        }
    }

    // Función para verificar si el usuario está baneado
    private fun checkIfUserIsBanned(userId: String, callback: (Boolean) -> Unit) {
        // Asumiendo que guardas el estado "banned" en la base de datos bajo el nodo "users"
        firebaseDatabase.reference.child("Users").child(userId).get()
            .addOnSuccessListener { snapshot ->
                // Verificamos si el campo "banned" existe y si es true
                val isBanned = snapshot.child("banned").getValue(Boolean::class.java) ?: false
                callback(isBanned) // Devolvemos true o false en función del valor encontrado
            }
            .addOnFailureListener {
                callback(false) // Si falla la lectura, asumimos que no está baneado
            }
    }

    // Función para obtener el rol del usuario y navegar
    private fun getUserRole(userId: String, navigateBasedOnRole: (String) -> Unit) {
        firebaseDatabase.reference.child("Users").child(userId).get()
            .addOnSuccessListener { snapshot ->
                val role = snapshot.child("role").getValue(Int::class.java) ?: -1

                when (role) {
                    0 -> navigateBasedOnRole("Admin")
                    1 -> navigateBasedOnRole("Seller")
                    2 -> navigateBasedOnRole("User")
                    else -> navigateBasedOnRole("Login")
                }
            }
            .addOnFailureListener {
                _loginError.value = "Failed to retrieve user role."
            }
    }
}
