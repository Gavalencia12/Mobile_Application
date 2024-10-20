package com.example.carhive.Presentation.seller.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.carhive.Presentation.seller.viewModel.CarAdapter
import com.example.carhive.Presentation.seller.viewModel.CrudViewModel
import com.example.carhive.databinding.FragmentCarsListBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CarsListFragment : Fragment() {

    // Binding for the fragment's layout
    private var _binding: FragmentCarsListBinding? = null
    private val binding get() = _binding!!

    // ViewModel to manage car data logic
    private val viewModel: CrudViewModel by viewModels()

    // Variables to determine which cars to display (sold/unsold/all)
    private var showSoldCars: Int? = null
    private var sectionTitle: String? = null

    // Inflate the fragment's view and bind it to the FragmentCarsListBinding object
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCarsListBinding.inflate(inflater, container, false)
        return binding.root
    }

    // This method is called after the view has been created. It handles UI setup and observers.
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Retrieve arguments for filtering cars and setting the section title
        showSoldCars = arguments?.getInt(ARG_SHOW_SOLD_CARS, -1)
        sectionTitle = arguments?.getString(ARG_SECTION_TITLE, "Car seller")

        // Update the section title TextView with the provided sectionTitle
        val sectionTextView: TextView = binding.section
        sectionTextView.text = sectionTitle

        // Handle the back button click to navigate back
        binding.ibtnBack.setOnClickListener {
            findNavController().popBackStack()  // Navigate back to the previous screen
        }

        // Set up the RecyclerView with a LinearLayoutManager for vertical scrolling
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Initialize the RecyclerView's adapter with an empty list and assign it to the RecyclerView
        val adapter = CarAdapter(emptyList(), requireActivity(), viewModel)
        binding.recyclerView.adapter = adapter

        // Observe the list of cars in the ViewModel and filter them based on their sold status
        viewModel.carList.observe(viewLifecycleOwner) { carList ->
            val filteredCars = when (showSoldCars) {
                1 -> carList.filter { it.sold }     // Show only sold cars
                0 -> carList.filter { !it.sold }    // Show only unsold cars
                -1 -> carList // Show all cars if -1 is passed
                else -> carList // Default case, show all cars
            }
            // Update the adapter with the filtered list of cars
            adapter.updateCars(filteredCars)
        }

        // Fetch all cars for the current user from the ViewModel
        viewModel.fetchCarsForUser()
    }

    // Companion object to pass arguments when creating a new instance of this fragment
    companion object {
        private const val ARG_SHOW_SOLD_CARS = "showSoldCars"
        private const val ARG_SECTION_TITLE = "sectionTitle"

        // Method to create a new instance of CarsListFragment with specified arguments
        fun newInstance(showSoldCars: Int, sectionTitle: String): CarsListFragment {
            return CarsListFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_SHOW_SOLD_CARS, showSoldCars)
                    putString(ARG_SECTION_TITLE, sectionTitle)
                }
            }
        }
    }

    // Clean up the binding when the view is destroyed to avoid memory leaks
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
