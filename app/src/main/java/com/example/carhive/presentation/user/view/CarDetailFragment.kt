package com.example.carhive.presentation.user.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.carhive.R
import com.example.carhive.presentation.user.adapter.ImagePagerAdapter
import com.example.carhive.databinding.FragmentUserHomeCardetailsBinding
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
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

        // Retrieve all car data fields from the Bundle
        val carId = arguments?.getString("carId")
        val carModel = arguments?.getString("carModel")
        val carPrice = arguments?.getString("carPrice")
        val carColor = arguments?.getString("carColor")
        val carDescription = arguments?.getString("carDescription")
        val carTransmission = arguments?.getString("carTransmission")
        val carFuelType = arguments?.getString("carFuelType")
        val carDoors = arguments?.getInt("carDoors")
        val carEngineCapacity = arguments?.getString("carEngineCapacity")
        val carLocation = arguments?.getString("carLocation")
        val carCondition = arguments?.getString("carCondition")
        val carPreviousOwners = arguments?.getInt("carPreviousOwners")
        val carMileage = arguments?.getString("carMileage")
        val carYear = arguments?.getString("carYear")
        val carImageUrls = arguments?.getStringArrayList("carImageUrls")
        val ownerId = arguments?.getString("ownerId")
        val buyerId = FirebaseAuth.getInstance().currentUser?.uid // Obtener el ID del usuario actual

        // Display car data in the views
        binding.carModel.text = carModel
        binding.carPrice.text = "$ $carPrice"
        binding.carColor.text = "$carColor"
        binding.carDescription.text = carDescription
        binding.carMileage.text = "$carMileage km"
        binding.carTransmission.text = "$carTransmission"
        binding.carFuelType.text = "$carFuelType"
        binding.carDoors.text = "$carDoors"
        binding.carEngineCapacity.text = "$carEngineCapacity cc"
        binding.carLocation.text = "$carLocation"
        binding.carCondition.text = "$carCondition"
        binding.carPreviousOwners.text = "$carPreviousOwners"
        binding.carYear.text = "$carYear"

        // Set up ViewPager2 with images
        carImageUrls?.let {
            val imagePagerAdapter = ImagePagerAdapter(it)
            binding.viewPager.adapter = imagePagerAdapter

            // Set up TabLayout (dots) with ViewPager2
            TabLayoutMediator(binding.tabLayout, binding.viewPager) { _, _ -> }.attach()

            // Set up arrows for navigation
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
        // Configurar el botón de mensaje
        binding.messageButton.setOnClickListener {
            val bundle = Bundle().apply {
                putString("carId", carId) // ID de la publicación
                putString("ownerId", ownerId) // ID del vendedor
                putString("buyerId", buyerId) // ID del comprador actual
            }
            findNavController().navigate(R.id.action_carDetailFragment_to_chat, bundle)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
