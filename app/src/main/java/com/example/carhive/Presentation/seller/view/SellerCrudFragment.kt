package com.example.carhive.Presentation.seller.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.carhive.Presentation.seller.viewModel.CarAdapter
import com.example.carhive.Presentation.seller.viewModel.CrudViewModel
import com.example.carhive.databinding.FragmentSellerCrudBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SellerCrudFragment : Fragment() {

    private var _binding: FragmentSellerCrudBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CrudViewModel by activityViewModels()
    private lateinit var carAdapter: CarAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSellerCrudBinding.inflate(inflater, container, false)
        return binding.root
    }

    private fun setupRecyclerView() {
        carAdapter = CarAdapter(emptyList(), requireActivity(), viewModel) // Pasa el ViewModel
        binding.recyclerViewCar.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = carAdapter
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Configura el RecyclerView con su adaptador, pasando el ViewModel
        carAdapter = CarAdapter(emptyList(), requireActivity(), viewModel) // Pasa el ViewModel
        binding.recyclerViewCar.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = carAdapter
        }

        // Observa los cambios en la lista de coches
        viewModel.carList.observe(viewLifecycleOwner) { cars ->
            carAdapter.updateCars(cars) // Actualiza el adaptador cuando cambien los datos
        }

        // Observa los errores
        viewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            // Mostrar un mensaje de error
            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
        }

        // Llama a la función para obtener los coches del usuario
        viewModel.fetchCarsForUser()

        // Llamar al modal cuando lo necesites, por ejemplo, con un botón
        binding.btnAddCar.setOnClickListener {
            showCarOptionsDialog()
        }
    }

    private fun showCarOptionsDialog() {
        val dialog = CrudDialogFragment()
        dialog.show(parentFragmentManager, "CarOptionsDialog")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
