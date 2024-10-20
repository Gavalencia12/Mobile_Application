package com.example.carhive.Presentation.user.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.carhive.Presentation.user.adapter.CarFavoritesAdapter
import com.example.carhive.Presentation.user.adapter.CarHomeAdapter
import com.example.carhive.Presentation.user.viewModel.FavoritesViewModel
import com.example.carhive.R
import com.example.carhive.databinding.FragmentUserFavoritesBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FavoritesFragment : Fragment() {

    private var _binding: FragmentUserFavoritesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FavoritesViewModel by viewModels()

    private lateinit var carAdapter: CarFavoritesAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserFavoritesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicializa el adaptador con el callback para eliminar de favoritos
        carAdapter = CarFavoritesAdapter(emptyList(), { car ->
            // Llama al ViewModel para eliminar el coche de favoritos
            viewModel.removeFavoriteCar(car)

        },{ car->
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
        })

        // Configura el RecyclerView
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = carAdapter
        }

        // Observa los coches favoritos en el ViewModel
        viewModel.favoriteCars.observe(viewLifecycleOwner) { favoriteCars ->
            carAdapter.updateCars(favoriteCars)
            // Muestra un mensaje si no hay favoritos
            if (favoriteCars.isEmpty()) {
                binding.emptyView.visibility = View.VISIBLE
            } else {
                binding.emptyView.visibility = View.GONE
            }
        }

        // Cargar los coches favoritos
        viewModel.fetchFavoriteCars()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
