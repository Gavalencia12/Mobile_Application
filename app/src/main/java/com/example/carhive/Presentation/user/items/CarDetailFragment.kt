package com.example.carhive.Presentation.user.items

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.carhive.Presentation.user.adapter.ImagePagerAdapter
import com.example.carhive.databinding.FragmentUserHomeCardetailsBinding
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CarDetailFragment : Fragment() {

    private var _binding: FragmentUserHomeCardetailsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserHomeCardetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Obtener los datos del coche desde el Bundle
        val carModel = arguments?.getString("carModel")
        val carPrice = arguments?.getString("carPrice")
        val carSpeed = arguments?.getString("carSpeed")
        val carColor = arguments?.getString("carColor")
        val carImageUrls = arguments?.getStringArrayList("carImageUrls")
        val carDescription = arguments?.getString("carDescription")

        // Mostrar los datos en las vistas
        binding.carModel.text = "$carModel"
        binding.carPrice.text = "$  $carPrice"
        binding.carColor.text = "Color: $carColor"
        binding.carSpeed.text = "Speed: $carSpeed"
        binding.carDescription.text = carDescription

        // Configurar el ViewPager2 con las imágenes
        carImageUrls?.let {
            val imagePagerAdapter = ImagePagerAdapter(it)
            binding.viewPager.adapter = imagePagerAdapter

            // Configurar el TabLayout (puntitos) con el ViewPager2
            TabLayoutMediator(binding.tabLayout, binding.viewPager) { _, _ -> }.attach()

            // Configurar las flechas para navegar
            binding.arrowLeft.setOnClickListener {
                val previousItem = binding.viewPager.currentItem - 1
                if (previousItem >= 0) {
                    binding.viewPager.currentItem = previousItem
                }
            }

            binding.arrowRight.setOnClickListener {
                val nextItem = binding.viewPager.currentItem + 1
                if (nextItem < carImageUrls.size) {
                    binding.viewPager.currentItem = nextItem
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
