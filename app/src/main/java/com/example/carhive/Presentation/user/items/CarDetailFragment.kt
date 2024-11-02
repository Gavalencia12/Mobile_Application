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

        // Retrieve all car data fields from the Bundle
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
        val carVin = arguments?.getString("carVin")
        val carPreviousOwners = arguments?.getInt("carPreviousOwners")
        val carMileage = arguments?.getString("carMileage")
        val carYear = arguments?.getString("carYear")
        val carImageUrls = arguments?.getStringArrayList("carImageUrls")

        // Display car data in the views
        binding.carModel.text = carModel
        binding.carPrice.text = "$ $carPrice"
        binding.carColor.text = "Color: $carColor"
        binding.carDescription.text = carDescription
        binding.carMileage.text = "Mileage: $carMileage"
        binding.carTransmission.text = "Transmission: $carTransmission"
        binding.carFuelType.text = "Fuel Type: $carFuelType"
        binding.carDoors.text = "Doors: $carDoors"
        binding.carEngineCapacity.text = "Engine Capacity: $carEngineCapacity"
        binding.carLocation.text = "Location: $carLocation"
        binding.carCondition.text = "Condition: $carCondition"
        binding.carVin.text = "VIN: $carVin"
        binding.carPreviousOwners.text = "Previous Owners: $carPreviousOwners"
        binding.carYear.text = "Year: $carYear"

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
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
