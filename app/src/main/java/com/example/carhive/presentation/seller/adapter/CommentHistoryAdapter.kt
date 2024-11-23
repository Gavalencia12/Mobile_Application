package com.example.carhive.presentation.seller.adapter

import android.view.LayoutInflater
import android.view.View
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
import java.util.Date
import java.util.Locale

class CommentHistoryAdapter (private val sellerId: String, // Añadimos el sellerId
private val comments: List<RatingSellerEntity>
) : RecyclerView.Adapter<CommentHistoryAdapter.CommentViewHolder>() {

    inner class CommentViewHolder(private val binding: ItemCommentsSellerBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(comment: RatingSellerEntity) {
            // Mostrar comentario y fecha
            binding.commentText.text = comment.comment
            val formattedDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(comment.date))
            binding.timeText.text = formattedDate

            // Mostrar estrellas de valoración
            updateStars(binding.starsSeller, comment.rating)

            loadUserDetails(comment.userId)

        }

        private fun loadUserDetails(userId: String) {
            val databaseReference = FirebaseDatabase.getInstance().getReference("Users/$userId")
            databaseReference.get().addOnSuccessListener { snapshot ->
                val user = snapshot.getValue(UserEntity::class.java)
                if (user != null) {
                    binding.firstNameText.text = user.firstName
                    binding.lastNameText.text = user.lastName
                    Glide.with(binding.root.context)
                        .load(user.imageUrl)
                        .placeholder(R.drawable.ic_profile)
                        .circleCrop()
                        .into(binding.userImageView)
                } else {
                    println("Usuario no encontrado para userId: $userId")
                }
            }.addOnFailureListener { e ->
                println("Error al cargar detalles del usuario: ${e.message}")
            }
        }

        // Método para actualizar las estrellas según el rating
        private fun updateStars(starsContainer: LinearLayout, rating: Int) {
            val starImages = listOf(
                starsContainer.findViewById<ImageView>(R.id.star1),
                starsContainer.findViewById<ImageView>(R.id.star2),
                starsContainer.findViewById<ImageView>(R.id.star3),
                starsContainer.findViewById<ImageView>(R.id.star4),
                starsContainer.findViewById<ImageView>(R.id.star5)
            )

            val filledStars = rating / 20

            for (i in starImages.indices) {
                if (i < filledStars) {
                    starImages[i].setImageResource(R.drawable.star) // Estrella llena
                } else {
                    starImages[i].setImageResource(R.drawable.nostar) // Estrella vacía
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val binding = ItemCommentsSellerBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return CommentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val comment = comments[position]
        holder.bind(comment)
    }

    override fun getItemCount(): Int = comments.size
}