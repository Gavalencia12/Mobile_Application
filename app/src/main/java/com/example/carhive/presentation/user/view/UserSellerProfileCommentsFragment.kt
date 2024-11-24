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
import com.example.carhive.data.model.RatingSellerEntity
import com.example.carhive.databinding.FragmentSellerProfileCommentsBinding
import com.example.carhive.presentation.user.adapter.CarAdapter
import com.example.carhive.presentation.user.adapter.RatingAdapter
import com.example.carhive.presentation.user.items.UserCommentDialog
import com.google.firebase.database.FirebaseDatabase

class UserSellerProfileCommentsFragment : Fragment() {

    private var _binding: FragmentSellerProfileCommentsBinding? = null
    private val binding get() = _binding!!
    private val ratingsList = mutableListOf<RatingSellerEntity>()
    private val carList = mutableListOf<CarEntity>()
    private lateinit var ratingsAdapter: RatingAdapter  // Aquí se declara el adaptador


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
            loadComments(it)  // Llamar a la función para cargar los comentarios
        }
        parentFragmentManager.setFragmentResultListener("REFRESH_COMMENTS", this) { _, _ ->
            sellerId?.let { loadComments(it) }
        }

        // Configuración del RecyclerView con el adaptador
        ratingsAdapter = RatingAdapter(ratingsList)
        binding.recyclerViewRatings.layoutManager = GridLayoutManager(requireContext(), 1)
        binding.recyclerViewRatings.adapter = ratingsAdapter

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
                    .error(R.drawable.ic_profile)
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

    private fun loadComments(sellerId: String) {
        val databaseReference = FirebaseDatabase.getInstance().getReference("RatingSeller/$sellerId")

        databaseReference.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                ratingsList.clear()
                var totalRating = 0
                for (commentSnapshot in snapshot.children) {
                    val comment = commentSnapshot.getValue(RatingSellerEntity::class.java)
                    comment?.let {
                        ratingsList.add(it)
                        totalRating += it.rating // Sumar la calificación
                    }
                }
                ratingsAdapter.notifyDataSetChanged()

                if (ratingsList.isNotEmpty()) {
                    binding.numComments.text = ratingsList.size.toString()
                    val averageRating = totalRating / ratingsList.size
                    updateStars(averageRating)

                    binding.recyclerViewRatings.visibility = View.VISIBLE
                    binding.Qualification.visibility = View.VISIBLE
                    binding.NotQualification.visibility = View.GONE
                } else {
                    binding.numComments.text = "0"
                    updateStars(0)
                    binding.recyclerViewRatings.visibility = View.GONE
                    binding.Qualification.visibility = View.GONE
                    binding.NotQualification.visibility = View.VISIBLE
                }

                ratingsAdapter = RatingAdapter(ratingsList)
                binding.recyclerViewRatings.adapter = ratingsAdapter
            } else {
                // No hay comentarios en la base de datos
                binding.numComments.text = "0"
                updateStars(0)
                binding.recyclerViewRatings.visibility = View.GONE
                binding.Qualification.visibility = View.GONE
                binding.NotQualification.visibility = View.VISIBLE
            }
        }
    }


    private fun updateStars(averageRating: Int) {
        val stars = when (averageRating) {
            in 0..19 -> 0
            in 20..39 -> 1
            in 40..59 -> 2
            in 60..79 -> 3
            in 80..99 -> 4
            else -> 5
        }

        val starImages = listOf(
            binding.star1,
            binding.star2,
            binding.star3,
            binding.star4,
            binding.star5
        )

        for (i in starImages.indices) {
            if (i < stars) {
                starImages[i].setImageResource(R.drawable.star)
            } else {
                starImages[i].setImageResource(R.drawable.nostar)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
