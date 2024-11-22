package com.example.carhive.presentation.user.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.carhive.presentation.user.adapter.CarFavoritesAdapter
import com.example.carhive.presentation.user.viewModel.FavoritesViewModel
import com.example.carhive.R
import com.example.carhive.databinding.FragmentUserFavoritesBinding
import dagger.hilt.android.AndroidEntryPoint

// Fragment for displaying and managing the user's list of favorite cars
@AndroidEntryPoint
class FavoritesFragment : Fragment() {

    private var _binding: FragmentUserFavoritesBinding? = null
    private val binding get() = _binding!! // Non-nullable binding property

    private val viewModel: FavoritesViewModel by viewModels() // ViewModel instance for favorites

    private lateinit var carAdapter: CarFavoritesAdapter // Adapter for the RecyclerView

    // Inflates the layout for the fragment
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserFavoritesBinding.inflate(inflater, container, false)
        return binding.root
    }

    // Sets up the RecyclerView, adapter, and observers when the view is created
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initializes the adapter with a callback for removing cars from favorites
        carAdapter = CarFavoritesAdapter(
            emptyList(),
            onDeleteFavoriteClick = { car ->
                viewModel.removeFavoriteCar(car) // Calls ViewModel to remove car from favorites
            },
            onCarClick = { car ->
                val bundle = Bundle().apply {
                    putString("carId", car.id)
                    putString("carModel", car.modelo)
                    putString("carPrice", car.price)
                    putString("carColor", car.color)
                    putString("carDescription", car.description)
                    putString("carTransmission", car.transmission)
                    putString("carFuelType", car.fuelType)
                    putInt("carDoors", car.doors)
                    putString("carEngineCapacity", car.engineCapacity)
                    putString("carLocation", car.location)
                    putString("carCondition", car.condition)
                    putString("carVin", car.vin)
                    putInt("carPreviousOwners", car.previousOwners)
                    putString("carYear", car.year)
                    putString("carMileage", car.mileage)
                    putString("carOwnerId", car.ownerId)
                    putStringArrayList("carImageUrls", ArrayList(car.imageUrls)) // Adds image URLs if available
                }
                // Navigate manually to CarDetailFragment, passing the bundle
                findNavController().navigate(R.id.action_userHomeFragment_to_carDetailFragment, bundle)
            }
        )

        // Configures the RecyclerView
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = carAdapter
        }

        // Observes favorite cars in the ViewModel and updates the UI accordingly
        viewModel.favoriteCars.observe(viewLifecycleOwner) { favoriteCars ->
            carAdapter.updateCars(favoriteCars) // Updates adapter data
            // Shows a message if there are no favorite cars
            binding.emptyView.visibility = if (favoriteCars.isEmpty()) View.VISIBLE else View.GONE
        }

        // Loads favorite cars from the ViewModel
        viewModel.fetchFavoriteCars()
    }

    // Cleans up the binding reference when the view is destroyed
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
