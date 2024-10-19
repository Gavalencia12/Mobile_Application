package com.example.carhive

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.example.carhive.Presentation.AuthViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val authViewModel: AuthViewModel by viewModels()
    private lateinit var navController: NavController

    // Definir las BottomNavigationViews
    lateinit var bottomNavigationViewUser: BottomNavigationView
    lateinit var bottomNavigationViewSeller: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Ocultar la ActionBar
        supportActionBar?.hide()

        // Configurar el NavHostFragment
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // Inicializar las BottomNavigationViews
        bottomNavigationViewUser = findViewById(R.id.bottom_navigation_user)
        bottomNavigationViewSeller = findViewById(R.id.bottom_navigation_seller)

        // Configurar el BottomNavigationView con el NavController
        NavigationUI.setupWithNavController(bottomNavigationViewUser, navController)
        NavigationUI.setupWithNavController(bottomNavigationViewSeller, navController)

        // Configurar la ventana para que sea fullscreen y transparente
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        window.statusBarColor = Color.TRANSPARENT

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }

        // Verificar la autenticación en el inicio
        checkAuthentication()

        // Configura el BottomNavigationView para navegar entre fragmentos del usuario
        bottomNavigationViewUser.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home -> {
                    navController.navigate(R.id.userHomeFragment)
                    true
                }
                R.id.profile -> {
                    navController.navigate(R.id.userProfileFragment)
                    true
                }
                else -> false
            }
        }

        // Configura el BottomNavigationView para navegar entre fragmentos del vendedor
        bottomNavigationViewSeller.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home -> {
                    navController.navigate(R.id.sellerHomeFragment)
                    true
                }
                R.id.profile -> {
                    navController.navigate(R.id.sellerHomeFragment)
                    true
                }

                else -> false
            }
        }

        // Controlar la visibilidad del BottomNavigationView según el fragmento actual
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.userHomeFragment, R.id.userProfileFragment, R.id.userHomeCarDetailFragment -> {
                    hideAllBottomNavigation()
                    showUserBottomNavigation()
                }
                R.id.sellerHomeFragment -> {
                    hideAllBottomNavigation()
                    showSellerBottomNavigation()
                }
                else -> hideAllBottomNavigation()
            }
        }
    }

    private fun checkAuthentication() {
        lifecycleScope.launch {
            // Observar el estado de autenticación
            authViewModel.isAuthenticated.collect { isAuthenticated ->
                if (isAuthenticated) {
                    // Si está autenticado, redirigir según el rol
                    authViewModel.userRole.collect { role ->
                        when (role) {
                            0 -> { // Admin
                                hideAllBottomNavigation()
                                navController.navigate(R.id.action_loginFragment_to_adminFragment)
                            }
                            1 -> { // Seller
                                hideAllBottomNavigation()
                                showSellerBottomNavigation()
                                navController.navigate(R.id.action_loginFragment_to_sellerFragment)
                            }
                            2 -> { // User
                                hideAllBottomNavigation()
                                showUserBottomNavigation()
                                navController.navigate(R.id.action_loginFragment_to_userhomeFragment)
                            }
                            else -> navController.navigate(R.id.action_loginFragment_to_loginFragment)
                        }
                    }
                } else {
                    // Si no está autenticado, ir al login
                    navController.navigate(R.id.action_loginFragment_to_loginFragment)
                }
            }
        }
    }

    // Métodos para mostrar/ocultar las BottomNavigationViews
    private fun showUserBottomNavigation() {
        bottomNavigationViewUser.visibility = View.VISIBLE
    }

    private fun showSellerBottomNavigation() {
        bottomNavigationViewSeller.visibility = View.VISIBLE
    }

    private fun hideAllBottomNavigation() {
        bottomNavigationViewUser.visibility = View.GONE
        bottomNavigationViewSeller.visibility = View.GONE
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}
