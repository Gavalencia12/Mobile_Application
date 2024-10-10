package com.example.carhive

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.example.carhive.Presentation.AuthViewModel
import com.example.carhive.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Configurar el NavHostFragment
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        NavigationUI.setupActionBarWithNavController(this, navHostFragment.navController)

        // Verificar la autenticación en el inicio
        checkAuthentication(navHostFragment)
    }

    private fun checkAuthentication(navHostFragment: NavHostFragment) {
        // Usar lifecycleScope para lanzar la coroutine
        lifecycleScope.launch {
            // Observar el estado de autenticación
            authViewModel.isAuthenticated.collect { isAuthenticated ->
                if (isAuthenticated) {
                    // Si está autenticado, redirigir según el rol
                    authViewModel.userRole.collect { role ->
                        when (role) {
                            0 -> navHostFragment.navController.navigate(R.id.action_loginFragment_to_adminFragment)
                            1 -> navHostFragment.navController.navigate(R.id.action_loginFragment_to_sellerFragment)
                            2 -> navHostFragment.navController.navigate(R.id.action_loginFragment_to_userhomeFragment)
                            else -> navHostFragment.navController.navigate(R.id.action_loginFragment_to_loginFragment)
                        }
                    }
                } else {
                    // Si no está autenticado, ir al login
                    navHostFragment.navController.navigate(R.id.action_loginFragment_to_loginFragment)
                }
            }
        }
    }
}
