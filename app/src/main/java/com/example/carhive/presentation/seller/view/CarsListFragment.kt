package com.example.carhive.presentation.seller.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AutoCompleteTextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.carhive.presentation.seller.viewModel.CarAdapter
import com.example.carhive.presentation.seller.viewModel.CrudViewModel
import com.example.carhive.databinding.FragmentCarsListBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CarsListFragment : Fragment() {

    private var _binding: FragmentCarsListBinding? = null // ViewBinding reference for the fragment's layout
    private val binding get() = _binding!! // Non-null binding reference

    private val viewModel: CrudViewModel by viewModels() // ViewModel for handling car data and business logic

    private var showSoldCars: Int? = null // Variable to indicate if sold cars should be shown
    private var sectionTitle: String? = null // Variable to hold the section title

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the fragment layout using ViewBinding
        _binding = FragmentCarsListBinding.inflate(inflater, container, false)
        return binding.root // Return the root view of the binding
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get the arguments passed to this fragment (if any) to determine if sold cars should be shown and the section title
        showSoldCars = arguments?.getInt(ARG_SHOW_SOLD_CARS, -1)
        sectionTitle = arguments?.getString(ARG_SECTION_TITLE, "Car seller")

        // Set the section title in the UI
        binding.section.text = sectionTitle
        // Set up the back button to navigate to the previous screen
        binding.ibtnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        // Set up the RecyclerView with a linear layout manager and initialize the adapter with an empty list
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        val adapter = CarAdapter(emptyList(), requireActivity(), viewModel)
        binding.recyclerView.adapter = adapter

        // Determine if only sold or unsold cars should be shown based on the showSoldCars argument
        val isSold = showSoldCars == 1

        // Call the `setupModelSearch` function to set up real-time search functionality for models
        viewModel.setupModelSearch(
            autoCompleteTextView = binding.autoCompleteModelSearch,
        )

        // Observe the carList LiveData from the ViewModel to update the UI when data changes
        viewModel.carList.observe(viewLifecycleOwner) { carList ->
            val approvedCars = carList

            // Filter the list based on the showSoldCars flag: show sold cars, unsold cars, or all cars
            val filteredCars = when (showSoldCars) {
                1 -> approvedCars.filter { it.sold }
                0 -> approvedCars.filter { !it.sold }
                else -> approvedCars
            }
            // Update the adapter with the filtered list
            adapter.updateCars(filteredCars)

            // Show an empty view if there are no cars to display, otherwise show the RecyclerView
            if (filteredCars.isEmpty()) {
                binding.recyclerView.visibility = View.GONE
                binding.emptyView.visibility = View.VISIBLE
            } else {
                binding.recyclerView.visibility = View.VISIBLE
                binding.emptyView.visibility = View.GONE
            }
        }

        // Fetch cars for the current user from the ViewModel when the fragment is created
        viewModel.fetchCarsForUser()
    }

    companion object {
        private const val ARG_SHOW_SOLD_CARS = "showSoldCars" // Constant for the sold cars argument
        private const val ARG_SECTION_TITLE = "sectionTitle" // Constant for the section title argument

        // Static function to create a new instance of CarsListFragment with arguments for showing sold cars and setting the section title
        fun newInstance(showSoldCars: Int, sectionTitle: String): CarsListFragment {
            return CarsListFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_SHOW_SOLD_CARS, showSoldCars)
                    putString(ARG_SECTION_TITLE, sectionTitle)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Clear the binding reference to prevent memory leaks
    }
}
