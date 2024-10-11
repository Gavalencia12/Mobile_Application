package com.example.carhive.Presentation.seller.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Configurar el RecyclerView
        carAdapter = CarAdapter()
        binding.recyclerView.adapter = carAdapter // Asegúrate de que tienes un RecyclerView en tu XML
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Observa la lista de coches en el ViewModel
        viewModel.cars.observe(viewLifecycleOwner) { cars ->
            carAdapter.submitList(cars) // Asegúrate de que tu adaptador tenga este método
        }

        // Cargar coches al inicio
        viewModel.loadCarsForUser()

        // Llamar al modal cuando lo necesites, por ejemplo, con un botón
        binding.btnAddCar.setOnClickListener {
            showCarOptionsDialog()
        }
    }

    private fun showCarOptionsDialog() {
        // Mostrar el modal
        val dialog = CrudDialogFragment()
        dialog.show(parentFragmentManager, "CarOptionsDialog")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
