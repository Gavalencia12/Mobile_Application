package com.example.carhive.presentation.user.items

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.carhive.data.model.RatingSellerEntity
import com.example.carhive.data.model.UserEntity
import com.bumptech.glide.Glide
import com.example.carhive.R
import com.example.carhive.databinding.DialogUserCommentBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class UserCommentDialog : DialogFragment() {
    companion object {
        private const val ARG_SELLER_ID = "sellerId"

        fun newInstance(sellerId: String): UserCommentDialog {
            val args = Bundle().apply {
                putString(ARG_SELLER_ID, sellerId)
            }
            return UserCommentDialog().apply {
                arguments = args
            }
        }
    }

    private var _binding: DialogUserCommentBinding? = null
    private val binding get() = _binding!!
    private var currentRating = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = DialogUserCommentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupDialogAppearance()
        setupStarRating()

        binding.btnComment.setOnClickListener {
            val comment = binding.etDescription.text.toString()
            if (currentRating == 0 || comment.isEmpty()) {
                Toast.makeText(requireContext(), "Calificación y comentario requeridos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val sellerId = arguments?.getString(ARG_SELLER_ID)
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            if (sellerId != null && userId != null) {
                val commentId = System.currentTimeMillis().toString()
                val ratingValue = currentRating * 20

                val ratingSeller = RatingSellerEntity(
                    rating = ratingValue,
                    comment = comment,
                    date = System.currentTimeMillis(),
                    userId = userId
                )

                val databaseRef = FirebaseDatabase.getInstance().getReference("RatingSeller/$sellerId/$commentId")
                databaseRef.setValue(ratingSeller).addOnSuccessListener {
                    Toast.makeText(requireContext(), "Comentario enviado", Toast.LENGTH_SHORT).show()
                    parentFragmentManager.setFragmentResult("REFRESH_COMMENTS", Bundle())
                    dismiss()
                }.addOnFailureListener {
                    Toast.makeText(requireContext(), "Error al enviar el comentario", Toast.LENGTH_SHORT).show()
                }
            }
        }

        val sellerId = arguments?.getString(ARG_SELLER_ID)
        sellerId?.let { loadUserData(it) }

        binding.btnCancel.setOnClickListener { dismiss() }
    }

    private fun setupStarRating() {
        val stars = listOf(binding.star1, binding.star2, binding.star3, binding.star4, binding.star5)
        stars.forEachIndexed { index, starImage ->
            starImage.setOnClickListener {
                currentRating = if (currentRating == index + 1) 0 else index + 1
                updateStarRating(currentRating)
            }
        }
    }

    private fun updateStarRating(rating: Int) {
        val stars = listOf(binding.star1, binding.star2, binding.star3, binding.star4, binding.star5)
        stars.forEachIndexed { index, starImage ->
            starImage.setImageResource(if (index < rating) R.drawable.star else R.drawable.nostar)
        }
    }

    private fun loadUserData(sellerId: String) {
        val databaseRef = FirebaseDatabase.getInstance().getReference("Users/$sellerId")
        databaseRef.get().addOnSuccessListener { snapshot ->
            val user = snapshot.getValue(UserEntity::class.java)
            user?.let {
                binding.sellername.text = "${it.firstName} ${it.lastName}"
                binding.selleremail.text = it.email
                binding.sellerrol.text = when (it.role) {
                    1 -> "Seller"
                    2 -> "Buyer"
                    0 -> "Administrator"
                    else -> "Unknown"
                }
                Glide.with(requireContext())
                    .load(it.imageUrl)
                    .placeholder(R.drawable.ic_profile)
                    .circleCrop()
                    .into(binding.ivUserImage)
            }
        }
    }

    private fun setupDialogAppearance() {
        dialog?.window?.apply {
            setBackgroundDrawableResource(R.drawable.dialog_background)
            setLayout(
                (resources.displayMetrics.widthPixels * 0.90).toInt(),
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
