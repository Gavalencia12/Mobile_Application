package com.example.carhive.presentation.user.items

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.carhive.data.model.UserEntity
import com.bumptech.glide.Glide
import com.example.carhive.R
import com.example.carhive.databinding.DialogUserCommentBinding
import com.example.carhive.presentation.user.view.SimpleUser
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
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogUserCommentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Configurar evento de estrellas
        setupStarRating()

        // Ejemplo de manejo de eventos en los botones del modal
        binding.btnSelectImage.setOnClickListener {
            val comment = binding.etDescription.text.toString()
            Toast.makeText(requireContext(), "Comentario enviado: $comment", Toast.LENGTH_SHORT).show()
            dismiss() // Cierra el modal
        }

        val sellerId = arguments?.getString(ARG_SELLER_ID)
        sellerId?.let {
            loadUserData(it)
        }
        binding.btnCancel.setOnClickListener {
            dismiss() // Cierra el modal sin acción
        }
    }
    private fun setupStarRating() {
        val stars = listOf(
            binding.star1,
            binding.star2,
            binding.star3,
            binding.star4,
            binding.star5
        )

        stars.forEachIndexed { index, starImage ->
            starImage.setOnClickListener {
                val selectedRating = index + 1
                // Verificar si la misma estrella fue presionada
                if (currentRating == selectedRating) {
                    // Si se vuelve a presionar la misma estrella, desmarcar
                    currentRating = 0
                    updateStarRating(0)
                } else {
                    // Si es una estrella diferente, actualizar la calificación
                    currentRating = selectedRating
                    updateStarRating(currentRating)
                }
            }
        }
    }

    private fun updateStarRating(rating: Int) {
        val stars = listOf(
            binding.star1,
            binding.star2,
            binding.star3,
            binding.star4,
            binding.star5
        )

        stars.forEachIndexed { index, starImage ->
            if (index < rating) {
                starImage.setImageResource(R.drawable.star)
            } else {
                starImage.setImageResource(R.drawable.nostar)
            }
        }

    }

    override fun onStart() {
        super.onStart()
        // Configurar el tamaño del diálogo
        dialog?.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.9).toInt(), // 90% del ancho de la pantalla
            (resources.displayMetrics.heightPixels * 0.6).toInt() // 60% de la altura de la pantalla
        )
        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        // Aplicar fondo redondeado
        dialog?.window?.setBackgroundDrawableResource(R.drawable.dialog_background)
    }
    private fun loadUserData(sellerId: String) {
        val databaseReference = FirebaseDatabase.getInstance().getReference("Users/$sellerId")

        databaseReference.get().addOnSuccessListener { snapshot ->
            val user = snapshot.getValue(UserEntity::class.java)  // Cargar UserEntity en lugar de SimpleUser
            user?.let {
                binding.sellername.text = "${it.firstName} ${it.lastName}"
                binding.selleremail.text = it.email
                val roleText = when (it.role) {
                    1 -> "Buyer"
                    0 -> "Administrator"
                    2 -> "User"
                    else -> "Not user"
                }
                binding.sellerrol.text = roleText
                Glide.with(requireContext())
                    .load(it.imageUrl)
                    .placeholder(R.drawable.ic_profile)
                    .error(R.drawable.ic_error)
                    .circleCrop()
                    .into(binding.ivUserImage)
            }
        }.addOnFailureListener {
            binding.sellername.text = getString(R.string.error_loading_user)
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
