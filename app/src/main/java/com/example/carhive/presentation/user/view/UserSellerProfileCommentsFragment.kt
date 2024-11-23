package com.example.carhive.presentation.user.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.example.carhive.R
import com.example.carhive.data.model.CarEntity
import com.example.carhive.databinding.FragmentSellerProfileCarsBinding
import com.example.carhive.databinding.FragmentSellerProfileCommentsBinding
import com.example.carhive.presentation.user.adapter.CarAdapter
import com.example.carhive.presentation.user.items.UserCommentDialog
import com.google.firebase.database.FirebaseDatabase

class UserSellerProfileCommentsFragment : Fragment() {


    private var _binding: FragmentSellerProfileCommentsBinding? = null
    private val binding get() = _binding!!
    private val carList = mutableListOf<CarEntity>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSellerProfileCommentsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sellerId = arguments?.getString("sellerId")
        val carAdapter = CarAdapter(carList) { car ->
            val bundle = Bundle().apply {
                putString("carId", car.id)
                putString("carOwnerId", car.ownerId)
            }
            findNavController().navigate(R.id.action_SellerProfileFragmentCars_to_carDetailFragment, bundle)
        }


        sellerId?.let {
            loadUserData(it)
            loadCars(it, carAdapter)
        }

        binding.ibtnBack.setOnClickListener {
            findNavController().navigate(R.id.action_UserSellerProfileFragmentCars_to_userHomeFragment)
        }

        binding.Carbutton.setOnClickListener {
            val bundle = Bundle().apply {
                putString("sellerId", sellerId)
            }
            findNavController().navigate(R.id.action_UserSellerProfileCommentsFragment_to_SellerProfileFragmentCars, bundle)
        }
        binding.commentSeller.setOnClickListener {
            sellerId?.let {
                val dialog = UserCommentDialog.newInstance(it)
                dialog.show(parentFragmentManager, "UserCommentDialog")
            }
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
}
