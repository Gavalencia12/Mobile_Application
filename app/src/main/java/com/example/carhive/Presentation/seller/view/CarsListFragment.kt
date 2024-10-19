package com.example.carhive.Presentation.seller.viewModel

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.carhive.databinding.FragmentCarsListBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CarsListFragment : Fragment() {

    // Binding for the fragment's layout
    private var _binding: FragmentCarsListBinding? = null
    private val binding get() = _binding!!

    // ViewModel to manage car data operations
    private val viewModel: CrudViewModel by viewModels()

    // Variable to determine which type of cars to show (sold/unsold/all)
    private var showSoldCars: Int? = null

    // Inflate the fragment's view
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCarsListBinding.inflate(inflater, container, false)
        return binding.root
    }

    // Logic for when the view is created
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Retrieve the argument to filter the cars based on their sold status
        showSoldCars = arguments?.getInt(ARG_SHOW_SOLD_CARS, -1)

        // Set up the RecyclerView with a LinearLayoutManager
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Initialize the adapter for the RecyclerView with an empty list
        val adapter = CarAdapter(emptyList(), requireActivity(), viewModel)
        binding.recyclerView.adapter = adapter

        // Observe the car list and filter based on the sold status
        viewModel.carList.observe(viewLifecycleOwner) { carList ->
            val filteredCars = when (showSoldCars) {
                1 -> carList.filter { it.sold }     // Show only sold cars
                0 -> carList.filter { !it.sold }    // Show only unsold cars
                -1 -> carList // Show all cars if -1
                else -> carList // Default case
            }
            // Update the adapter with the filtered list of cars
            adapter.updateCars(filteredCars)
        }

        // Fetch all cars for the current user
        viewModel.fetchCarsForUser()
    }

    // Companion object to pass arguments when creating an instance of the fragment
    companion object {
        private const val ARG_SHOW_SOLD_CARS = "showSoldCars"

        // Method to create a new instance of CarsListFragment with an argument
        fun newInstance(showSoldCars: Int): CarsListFragment {
            return CarsListFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_SHOW_SOLD_CARS, showSoldCars)
                }
            }
        }
    }

    // Clean up the binding when the view is destroyed
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
