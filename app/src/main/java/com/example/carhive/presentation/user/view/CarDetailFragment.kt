package com.example.carhive.presentation.user.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.carhive.R
import com.example.carhive.databinding.FragmentUserHomeCardetailsBinding
import com.example.carhive.presentation.user.adapter.ImagePagerAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
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

        // Retrieve car data from arguments
        val carId = arguments?.getString("carId")
        val carBrand = arguments?.getString("carBrand")
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
        val ownerId = arguments?.getString("carOwnerId")
        val buyerId = FirebaseAuth.getInstance().currentUser?.uid

        // Display car data
        binding.carModel.text = carModel
        binding.carBrand.text = carBrand
        binding.carPrice.text = "$ $carPrice"
        binding.carColor.text = carColor
        binding.carDescription.text = carDescription
        binding.carMileage.text = "$carMileage km"
        binding.carTransmission.text = carTransmission
        binding.carFuelType.text = carFuelType
        binding.carDoors.text = "$carDoors"
        binding.carEngineCapacity.text = "$carEngineCapacity cc"
        binding.carLocation.text = carLocation
        binding.carCondition.text = carCondition
        binding.carPreviousOwners.text = "$carPreviousOwners"
        binding.carYear.text = carYear

        // Load owner data and display it in the profile section
        ownerId?.let {
            loadUserData(it)
        }

        // Set up ViewPager2 with images
        carImageUrls?.let {
            val imagePagerAdapter = ImagePagerAdapter(it)
            binding.viewPager.adapter = imagePagerAdapter

            // Set up TabLayout with ViewPager2
            TabLayoutMediator(binding.tabLayout, binding.viewPager) { _, _ -> }.attach()

            // Arrows navigation
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

        // Set up message button
        binding.messageButton.setOnClickListener {
            val bundle = Bundle().apply {
                putString("carId", carId)
                putString("ownerId", ownerId)
                putString("buyerId", buyerId)
            }
            findNavController().navigate(R.id.action_carDetailFragment_to_chat, bundle)
        }
        binding.tvName.setOnClickListener {
            val sellerId = ownerId // Asegúrate de que este ID ya está disponible
            val bundle = Bundle().apply {
                putString("sellerId", sellerId)
            }
            findNavController().navigate(R.id.action_carDetailFragment_to_SellerProfileFragmentCars, bundle)
        }

    }

    /**
     * Fetches user data using the ownerId and displays it in the profile section.
     */
    private fun loadUserData(ownerId: String) {
        val databaseReference = FirebaseDatabase.getInstance().getReference("Users/$ownerId")

        databaseReference.get().addOnSuccessListener { snapshot ->
            val user = snapshot.getValue(SimpleUser::class.java) // Usar SimpleUser para evitar problemas con role
            user?.let {
                // Concatenate firstName and lastName to display full name
                val fullName = "${it.firstName} ${it.lastName}"
                binding.tvName.text = fullName

                Glide.with(requireContext())
                    .load(it.imageUrl)
                    .placeholder(R.drawable.ic_profile)
                    .error(R.drawable.ic_error)
                    .circleCrop()
                    .into(binding.ivProfile)
            }
        }.addOnFailureListener {
            binding.tvName.text = getString(R.string.error_loading_user)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

/**
 * Simplified User class to avoid role deserialization issues.
 */
data class SimpleUser(
    val firstName: String = "",
    val lastName: String = "",
    val imageUrl: String = ""
)
