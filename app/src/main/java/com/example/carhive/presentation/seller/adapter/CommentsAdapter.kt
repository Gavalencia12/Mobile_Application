package com.example.carhive.presentation.seller.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.carhive.data.model.RatingSellerEntity
import com.example.carhive.R
import com.example.carhive.data.model.CarEntity
import com.example.carhive.data.model.UserEntity
import com.example.carhive.databinding.ItemCommentBinding
import com.example.carhive.databinding.ItemCommentsSellerBinding
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class CommentsAdapter(
    private val sellerId: String,
    private val comments: MutableList<RatingSellerEntity>,
    private val onRatingCalculated: (Int) -> Unit
) : RecyclerView.Adapter<CommentsAdapter.CommentViewHolder>() {

    fun updateComments(newComments: List<RatingSellerEntity>) {
        comments.clear()
        comments.addAll(newComments)
        notifyDataSetChanged()
        calculateAndNotifyAverageRating()
    }
    private fun calculateAndNotifyAverageRating() {
        if (comments.isNotEmpty()) {
            val totalRating = comments.sumOf { it.rating }
            val averageRating = totalRating / comments.size
            val percentage = (averageRating * 100) / 100
            onRatingCalculated(percentage)
        } else {
            onRatingCalculated(0)
        }
    }

    inner class CommentViewHolder(private val binding: ItemCommentBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(comment: RatingSellerEntity) {
            binding.commentText.text = comment.comment
            val formattedDate =
                SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(comment.date))

            updateStars(binding.starsSeller, comment.rating)

            loadUserDetails(comment.userId)

        }

        private fun loadUserDetails(userId: String) {
            val databaseReference = FirebaseDatabase.getInstance().getReference("Users/$userId")
            databaseReference.get().addOnSuccessListener { snapshot ->
                val user = snapshot.getValue(UserEntity::class.java)
                if (user != null) {
                    binding.firstNameText.text = "${user.firstName} ${user.lastName}"
                    Glide.with(binding.root.context)
                        .load(user.imageUrl)
                        .placeholder(R.drawable.ic_profile)
                        .circleCrop()
                        .into(binding.userImageView)
                }
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

            val filledStars = rating / 20

            for (i in starImages.indices) {
                if (i < filledStars) {
                    starImages[i].setImageResource(R.drawable.star)
                } else {
                    starImages[i].setImageResource(R.drawable.nostar)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val binding = ItemCommentBinding.inflate(
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