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
import com.example.carhive.presentation.AuthViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val authViewModel: AuthViewModel by viewModels() // Using Hilt to inject the AuthViewModel
    private lateinit var navController: NavController

    // Define BottomNavigationViews for User and Seller
    lateinit var bottomNavigationViewUser: BottomNavigationView
    lateinit var bottomNavigationViewSeller: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportActionBar?.hide() // Hide the action bar for a full-screen look

        // Set up the NavHostFragment to manage navigation
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // Initialize the BottomNavigationViews for both user and seller roles
        bottomNavigationViewUser = findViewById(R.id.bottom_navigation_user)
        bottomNavigationViewSeller = findViewById(R.id.bottom_navigation_seller)

        // Connect the BottomNavigationViews to the NavController for navigation
        NavigationUI.setupWithNavController(bottomNavigationViewUser, navController)
        NavigationUI.setupWithNavController(bottomNavigationViewSeller, navController)

        // Set the window to full-screen with a transparent status bar
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        window.statusBarColor = Color.TRANSPARENT

        // If the device is running Android Marshmallow or higher, adjust the status bar icon color
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }

        // Check authentication status when the activity is created
        checkAuthentication()

        // Configure the user navigation based on the selected item in the bottom navigation
        bottomNavigationViewUser.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home -> {
                    navController.navigate(R.id.userHomeFragment)
                    true
                }
                R.id.chats -> {
                    navController.navigate(R.id.userMessages)
                    true
                }
                R.id.favorites -> {
                    navController.navigate(R.id.favoritesFragment)
                    true
                }
                R.id.notifications -> {
                    navController.navigate(R.id.notificationsFragment)
                    true
                }
                R.id.profile -> {
                    navController.navigate(R.id.userProfileFragment)
                    true
                }
                else -> false
            }
        }

        // Configure the seller navigation based on the selected item in the bottom navigation
        bottomNavigationViewSeller.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home -> {
                    navController.navigate(R.id.sellerHomeFragment)
                    true
                }
                R.id.chats -> {
                    navController.navigate(R.id.sellerInterestedUsersFragment)
                    true
                }
                R.id.crud -> {
                    navController.navigate(R.id.sellerCrudFragment)
                    true
                }
                R.id.notifications -> {
                    navController.navigate(R.id.notificationsSellerFragment)
                    true
                }
                R.id.profile -> {
                    navController.navigate(R.id.sellerProfileFragment)
                    true
                }
                else -> false
            }
        }

        // Adjust bottom navigation visibility based on the current destination (fragment)
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.userHomeFragment, R.id.userProfileFragment, R.id.favoritesFragment, R.id.userHomeCarDetailFragment, R.id.userMessages, R.id.notificationsFragment -> {
                    hideAllBottomNavigation()
                    showUserBottomNavigation()
                }
                R.id.sellerHomeFragment, R.id.sellerCrudFragment, R.id.sellerProfileFragment, R.id.sellerInterestedUsersFragment, R.id.notificationsSellerFragment -> {
                    hideAllBottomNavigation() // Hide other navigations and show the seller navigation
                    showSellerBottomNavigation()
                }
                else -> hideAllBottomNavigation() // Hide all navigations for other fragments
            }
        }
    }

    // Method to check authentication and redirect based on role
    private fun checkAuthentication() {
        lifecycleScope.launch {
            // Observe the authentication state from the AuthViewModel
            authViewModel.isAuthenticated.collect { isAuthenticated ->
                if (isAuthenticated) {
                    // If authenticated, observe the user role and navigate accordingly
                    authViewModel.userRole.collect { role ->
                        when (role) {
                            0 -> { // Admin role
                                hideAllBottomNavigation() // Hide navigation as it's not needed for admin
                                navController.navigate(R.id.action_loginFragment_to_adminFragment)
                            }
                            1 -> { // Seller role
                                hideAllBottomNavigation()
                                showSellerBottomNavigation() // Show seller navigation
                                navController.navigate(R.id.action_loginFragment_to_sellerFragment)
                            }
                            2 -> { // User role
                                hideAllBottomNavigation()
                                showUserBottomNavigation() // Show user navigation
                                navController.navigate(R.id.action_loginFragment_to_userhomeFragment)
                            }
                            else -> navController.navigate(R.id.action_loginFragment_to_loginFragment) // Default case to login
                        }
                    }
                } else {
                    // Navigate to login screen if not authenticated
                    navController.navigate(R.id.action_loginFragment_to_loginFragment)
                }
            }
        }
    }

    // Methods to control visibility of BottomNavigationViews
    private fun showUserBottomNavigation() {
        bottomNavigationViewUser.visibility = View.VISIBLE
    }

    private fun showSellerBottomNavigation() {
        bottomNavigationViewSeller.visibility = View.VISIBLE
    }

    private fun hideAllBottomNavigation() {
        bottomNavigationViewUser.visibility = View.GONE
        bottomNavigationViewSeller.visibility = View.GONE
        bottomNavigationViewUser.menu.findItem(R.id.home).isChecked = true
        bottomNavigationViewSeller.menu.findItem(R.id.home).isChecked = true

    }

    // Handle the back button navigation
    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}
