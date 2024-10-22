package com.example.carhive.Presentation.seller.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.carhive.Presentation.seller.viewModel.CarAdapter
import com.example.carhive.Presentation.seller.viewModel.CarFavoritesAdapter
import com.example.carhive.Presentation.seller.viewModel.CrudViewModel
import com.example.carhive.databinding.FragmentSellerFavoritesBinding

class SellerFavoritesFragment : Fragment() {

    private var _binding: FragmentSellerFavoritesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CrudViewModel by activityViewModels()
    private lateinit var carAdapter: CarFavoritesAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSellerFavoritesBinding.inflate(inflater, container, false)
        return binding.root
    }

    private fun setupRecyclerView() {
        carAdapter = CarFavoritesAdapter(emptyList(), emptyMap(), requireActivity(), viewModel)
        binding.recyclerViewCar.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = carAdapter
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()

        // Observar la lista de coches
        viewModel.carList.observe(viewLifecycleOwner) { cars ->
            if (cars.isEmpty()) {
                // Mostrar el mensaje cuando no hay coches
                binding.recyclerViewCar.visibility = View.GONE
                binding.emptyView.visibility = View.VISIBLE
            } else {
                // Ocultar el mensaje y mostrar el RecyclerView si hay coches
                binding.recyclerViewCar.visibility = View.VISIBLE
                binding.emptyView.visibility = View.GONE
                viewModel.favoriteCounts.observe(viewLifecycleOwner) { favoriteCounts ->
                    carAdapter.updateCars(cars, favoriteCounts)
                }
            }
        }

        binding.ibtnBack.setOnClickListener {
            findNavController().popBackStack()  // Navigate back to the previous screen
        }

        viewModel.fetchCarsForUser()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
