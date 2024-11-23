package com.example.carhive.presentation.user.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.carhive.data.model.CarEntity
import com.example.carhive.presentation.user.adapter.CarAdapter
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.carhive.R
import com.example.carhive.databinding.FragmentSellerProfileCarsBinding
import com.google.firebase.crashlytics.internal.model.CrashlyticsReport
import com.google.firebase.database.FirebaseDatabase
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SellerProfileFragmentCars : Fragment() {

    private var _binding: FragmentSellerProfileCarsBinding? = null
    private val binding get() = _binding!!
    private val carList = mutableListOf<CarEntity>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSellerProfileCarsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sellerId = arguments?.getString("sellerId")

        binding.recyclerViewCars.layoutManager = GridLayoutManager(requireContext(), 2) // 2 columnas
        val carAdapter = CarAdapter(carList) { car ->
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
                putInt("carPreviousOwners", car.previousOwners)
                putString("carMileage", car.mileage)
                putString("carYear", car.year)
                putStringArrayList("carImageUrls", ArrayList(car.imageUrls))
                putString("carOwnerId", car.ownerId)
            }
            findNavController().navigate(R.id.action_SellerProfileFragmentCars_to_carDetailFragment, bundle)
        }
        binding.recyclerViewCars.adapter = carAdapter
        binding.recyclerViewCars.setHasFixedSize(true)

        sellerId?.let {
            loadUserData(it)
            loadCars(it, carAdapter)
        }

        binding.ibtnBack.setOnClickListener {
            findNavController().navigate(R.id.action_SellerProfileFragmentCars_to_userHomeFragment)
        }

        binding.Commentbutton.setOnClickListener {
            val bundle = Bundle().apply {
                putString("sellerId", sellerId)
            }
            findNavController().navigate(R.id.action_SellerProfileFragmentCars_to_UserSellerProfileCommentsFragment, bundle)
        }
    }




    private fun loadUserData(sellerId: String) {
        val databaseReference = FirebaseDatabase.getInstance().getReference("Users/$sellerId")

        databaseReference.get().addOnSuccessListener { snapshot ->
            val user = snapshot.getValue(SimpleUser::class.java)
            user?.let {
                binding.profileName.text = "${it.firstName} ${it.lastName}"
                Glide.with(requireContext())
                    .load(it.imageUrl)
                    .placeholder(R.drawable.ic_profile)
                    .error(R.drawable.ic_error)
                    .circleCrop()
                    .into(binding.profileImage)
            }
        }.addOnFailureListener {
            binding.profileName.text = getString(R.string.error_loading_user)
        }
    }

    private fun loadCars(sellerId: String, adapter: CarAdapter) {
        val databaseReference = FirebaseDatabase.getInstance().getReference("Car/$sellerId")

        databaseReference.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                carList.clear()
                var approvedCount = 0
                for (carSnapshot in snapshot.children) {
                    val car = carSnapshot.getValue(CarEntity::class.java)
                    if (car != null && car.approved) {
                        carList.add(car)
                        approvedCount++
                    }
                }
                adapter.notifyDataSetChanged()

                binding.numCars.text = approvedCount.toString()
            } else {
                binding.numCars.text = "0"
            }
        }.addOnFailureListener { exception ->
            binding.numCars.text = "0"
        }
    }






    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
