package com.example.carhive.presentation.user.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.carhive.R
import com.example.carhive.data.model.RatingSellerEntity
import com.example.carhive.data.model.UserEntity
import com.example.carhive.databinding.ItemCommentsSellerBinding
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.*

class RatingAdapter(private val ratings: List<RatingSellerEntity>) : RecyclerView.Adapter<RatingAdapter.RatingViewHolder>() {
    private val sortedRatings = ratings.sortedByDescending { it.date } // Ordenar por fecha descendente

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RatingViewHolder {
        val binding = ItemCommentsSellerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RatingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RatingViewHolder, position: Int) {
        val rating = sortedRatings[position] // Usar la lista ordenada
        holder.bind(rating)
        holder.loadUserDetails(rating.userId) // Cargar detalles del usuario
    }

    override fun getItemCount(): Int = sortedRatings.size // Usar el tamaño de la lista ordenada

    inner class RatingViewHolder(private val binding: ItemCommentsSellerBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(rating: RatingSellerEntity) {
            // Mostrar el comentario y la fecha
            binding.commentText.text = rating.comment
            val formattedDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(rating.date))
            binding.timeText.text = formattedDate
            updateStars(binding.starsSeller, rating.rating) // Asegúrate de que el modelo tenga un campo `rating`
        }

        fun loadUserDetails(userId: String) {
            // Referencia a Firebase para obtener detalles del usuario
            val databaseReference = FirebaseDatabase.getInstance().getReference("Users/$userId")
            databaseReference.get().addOnSuccessListener { snapshot ->
                val user = snapshot.getValue(UserEntity::class.java)
                if (user != null) {
                    // Mostrar datos del usuario
                    binding.firstNameText.text = user.firstName
                    binding.lastNameText.text = user.lastName
                    Glide.with(binding.root.context)
                        .load(user.imageUrl)
                        .placeholder(R.drawable.ic_profile) // Imagen por defecto mientras carga
                        .error(R.drawable.ic_error) // Imagen de error si no carga
                        .circleCrop()
                        .into(binding.userImageView)
                }
            }.addOnFailureListener {
                binding.firstNameText.text = binding.root.context.getString(R.string.error_loading_user)
                binding.lastNameText.text = ""
            }
        }
        private fun updateStars(starsContainer: LinearLayout, rating: Int) {
            val starImages = listOf(
                starsContainer.findViewById<ImageView>(R.id.star1),
                starsContainer.findViewById<ImageView>(R.id.star2),
                starsContainer.findViewById<ImageView>(R.id.star3),
                starsContainer.findViewById<ImageView>(R.id.star4),
                starsContainer.findViewById<ImageView>(R.id.star5)
            )

            // Calcular el número de estrellas llenas (rating dividido entre 20)
            val filledStars = rating / 20

            // Actualizar las estrellas según el número calculado
            for (i in starImages.indices) {
                if (i < filledStars) {
                    starImages[i].setImageResource(R.drawable.star) // Estrella llena
                } else {
                    starImages[i].setImageResource(R.drawable.nostar) // Estrella vacía
                }
            }
        }


    }
}
