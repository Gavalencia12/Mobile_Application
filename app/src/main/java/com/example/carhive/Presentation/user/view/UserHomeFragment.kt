package com.example.carhive.Presentation.user.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.carhive.Presentation.user.adapter.CarHomeAdapter
import com.example.carhive.Presentation.user.viewModel.UserViewModel
import com.example.carhive.R
import com.example.carhive.databinding.FragmentUserHomeBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UserHomeFragment : Fragment() {

    private var _binding: FragmentUserHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: UserViewModel by viewModels()

    private lateinit var carAdapter: CarHomeAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        carAdapter = CarHomeAdapter(emptyList()) { car ->
            // Crea un Bundle con los datos del coche
            val bundle = Bundle().apply {
                putString("carModel", car.modelo)
                putString("carMarca", car.addOn)
                putString("carPrice", car.price)
                putString("carColor", car.color)
                putString("carSpeed", car.speed)
                putString("carDescription", car.description)
                putStringArrayList("carImageUrls", ArrayList(car.imageUrls)) // Si tiene imágenes
            }

            // Navegar manualmente a CarDetailFragment pasando el Bundle
            findNavController().navigate(R.id.action_userHomeFragment_to_carDetailFragment, bundle)
        }

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = carAdapter
        }

        viewModel.carList.observe(viewLifecycleOwner) { cars ->
            carAdapter.updateCars(cars)
        }

        viewModel.fetchCars()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}