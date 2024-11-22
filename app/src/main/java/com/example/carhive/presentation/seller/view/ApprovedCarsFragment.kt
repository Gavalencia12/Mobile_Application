package com.example.carhive.presentation.seller.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.carhive.presentation.seller.viewModel.CarAdapter
import com.example.carhive.presentation.seller.viewModel.CrudViewModel
import com.example.carhive.databinding.FragmentApprovedCarsBinding
import com.example.carhive.presentation.seller.items.CrudDialogFragment

class ApprovedCarsFragment : Fragment() {

    private var _binding: FragmentApprovedCarsBinding? = null // ViewBinding for the fragment
    private val binding get() = _binding!! // Getter for the binding object

    private val viewModel: CrudViewModel by activityViewModels() // Shared ViewModel for CRUD operations
    private lateinit var carAdapter: CarAdapter // Adapter for the RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentApprovedCarsBinding.inflate(inflater, container, false) // Inflate the binding layout
        return binding.root // Return the root view of the binding
    }

    // Function to set up the RecyclerView
    private fun setupRecyclerView() {
        carAdapter = CarAdapter(emptyList(), requireActivity(), viewModel) // Initialize the adapter with an empty list
        binding.recyclerViewCar.apply {
            layoutManager = LinearLayoutManager(context) // Set layout manager
            adapter = carAdapter // Set the adapter
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set up the RecyclerView
        setupRecyclerView()

        // Observe changes in the car list from the ViewModel
        viewModel.carList.observe(viewLifecycleOwner) { cars ->
            // Filter cars with `approved` set to false
            val unapprovedCars = cars.filter { car -> car.approved == false }
            carAdapter.updateCars(unapprovedCars) // Update the adapter with filtered data
        }

        // Observe error messages from the ViewModel
        viewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
        }

        // Fetch cars for the current user from the ViewModel
        viewModel.fetchCarsForUser()

        // Configure AutoCompleteTextView for real-time search
        val searchAutoCompleteTextView = binding.autoCompleteModelSearch
        viewModel.setupModelSearch(searchAutoCompleteTextView)

        binding.ibtnBack.setOnClickListener {
            findNavController().popBackStack()  // Navigate back to the previous screen
        }
    }

    // Function to display the car options dialog
    private fun showCarOptionsDialog() {
        val dialog = CrudDialogFragment() // Create an instance of the dialog
        dialog.show(parentFragmentManager, "CarOptionsDialog") // Show the dialog
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Clear the binding reference to avoid memory leaks
    }
}
