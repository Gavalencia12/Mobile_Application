package com.example.carhive.presentation.seller.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.carhive.presentation.seller.viewModel.CarFavoritesAdapter
import com.example.carhive.presentation.seller.viewModel.CrudViewModel
import com.example.carhive.databinding.FragmentSellerFavoritesBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SellerFavoritesFragment : Fragment() {

    private var _binding: FragmentSellerFavoritesBinding? = null // ViewBinding reference for the fragment's layout
    private val binding get() = _binding!! // Non-null binding reference

    private val viewModel: CrudViewModel by activityViewModels() // ViewModel for handling car data and business logic
    private lateinit var carAdapter: CarFavoritesAdapter // Adapter for displaying car favorites in RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the fragment layout using ViewBinding
        _binding = FragmentSellerFavoritesBinding.inflate(inflater, container, false)
        return binding.root // Return the root view of the binding
    }

    // Function to set up the RecyclerView with the favorites adapter
    private fun setupRecyclerView() {
        // Initialize the adapter with an empty list and empty map for favorite counts
        carAdapter = CarFavoritesAdapter(emptyList(), emptyMap(), requireActivity(), viewModel)
        binding.recyclerViewCar.apply {
            layoutManager = LinearLayoutManager(context) // Set a linear layout manager for the RecyclerView
            adapter = carAdapter // Set the adapter
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize the RecyclerView
        setupRecyclerView()

        // Set up model search with real-time suggestions in the AutoCompleteTextView
        viewModel.setupModelSearch(
            autoCompleteTextView = binding.autoCompleteModelSearch
        )

        // Observe the carList LiveData from the ViewModel to update the UI when data changes
        viewModel.carList.observe(viewLifecycleOwner) { cars ->
            val approvedCars = cars.filter { it.approved }


            if (approvedCars.isEmpty()) {
                // Show the empty view if no cars are available
                binding.recyclerViewCar.visibility = View.GONE
                binding.emptyView.visibility = View.VISIBLE
            } else {
                // Show the RecyclerView and hide the empty view if cars are available
                binding.recyclerViewCar.visibility = View.VISIBLE
                binding.emptyView.visibility = View.GONE

                // Observe the favoriteCounts LiveData to update favorite counts in the adapter
                viewModel.favoriteCounts.observe(viewLifecycleOwner) { favoriteCounts ->
                    carAdapter.updateCars(approvedCars, favoriteCounts) // Update the adapter with cars and favorite counts
                }
            }
        }

        // Set up the back button to navigate to the previous screen
        binding.ibtnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        // Fetch cars for the current user when the fragment is created
        viewModel.fetchCarsForUser()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Clear the binding reference to prevent memory leaks
    }
}
