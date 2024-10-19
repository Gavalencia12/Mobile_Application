package com.example.carhive.Presentation.seller.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import com.example.carhive.Data.model.UserEntity
import com.example.carhive.Presentation.seller.viewModel.CarsListFragment
import com.example.carhive.Presentation.seller.viewModel.SellerHomeViewModel
import com.example.carhive.R
import com.example.carhive.databinding.FragmentSellerHomeBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SellerHomeFragment : Fragment() {

    // Binding for the fragment's layout
    private var _binding: FragmentSellerHomeBinding? = null
    private val binding get() = _binding!!

    // ViewModel to manage seller-related data
    private val viewModel: SellerHomeViewModel by viewModels()

    // Inflate the fragment's view
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSellerHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    // Handle logic after the view has been created
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Configurar el BottomNavigationView con el NavController
        val navController = findNavController()
        NavigationUI.setupWithNavController(binding.bottomNavigationSeller, navController)

        // Observe user data and update UI when data is available
        viewModel.userData.observe(viewLifecycleOwner) { result ->
            result.onSuccess { user ->
                if (user != null) {
                    // Set the username in the TextView
                    GetUsername(user)
                }
            }.onFailure {
                // Show a toast message if user data retrieval fails
                Toast.makeText(requireContext(), "Error fetching user data", Toast.LENGTH_SHORT).show()
            }
        }

        // Observe the total car count and update the TextView
        viewModel.totalCarsCount.observe(viewLifecycleOwner) { count ->
            binding.tvAllCarsCount.text = count.toString()
        }

        // Observe the sold car count and update the TextView
        viewModel.soldCarsCount.observe(viewLifecycleOwner) { count ->
            binding.tvSoldCarsCount.text = count.toString()
        }

        // Observe the unsold car count and update the TextView
        viewModel.unsoldCarsCount.observe(viewLifecycleOwner) { count ->
            binding.tvUnsoldCarsCount.text = count.toString()
        }

        // Fetch user and car data when the view is created
        viewModel.fetchUserData()
        viewModel.fetchCarsForUser()

        // Set up click listeners for the car count cards
        binding.cardAllCars.setOnClickListener {
            // Navigate to the list of all cars
            findNavController().navigate(
                R.id.carsListFragment,
                CarsListFragment.newInstance(-1).arguments // Passing -1 to show all cars
            )
        }

        binding.cardSoldCars.setOnClickListener {
            // Navigate to the list of sold cars
            findNavController().navigate(
                R.id.carsListFragment,
                CarsListFragment.newInstance(1).arguments // Show sold cars
            )
        }

        binding.cardUnsoldCars.setOnClickListener {
            // Navigate to the list of unsold cars
            findNavController().navigate(
                R.id.carsListFragment,
                CarsListFragment.newInstance(0).arguments // Show unsold cars
            )
        }

        // Placeholder listener for the favorite cars card
        binding.cardFavoriteCars.setOnClickListener {
            Toast.makeText(requireContext(), "Favorite cars - Card 4", Toast.LENGTH_SHORT).show()
        }
    }

    // Function to set the user's name in the UI
    private fun GetUsername(user: UserEntity) {
        binding.tvUsername.text = "${user.firstName} ${user.lastName}"
    }

    // Clean up the binding when the view is destroyed
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
