package com.example.carhive.presentation.user.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.carhive.databinding.ItemRecommendationBinding

class RecommendationsAdapter(private var recommendations: List<String>) :
    RecyclerView.Adapter<RecommendationsAdapter.RecommendationViewHolder>() {

    inner class RecommendationViewHolder(private val binding: ItemRecommendationBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(text: String) {
            binding.tvRecommendationText.text = text
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecommendationViewHolder {
        val binding = ItemRecommendationBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return RecommendationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecommendationViewHolder, position: Int) {
        holder.bind(recommendations[position])
    }

    override fun getItemCount(): Int = recommendations.size

    // Función para actualizar las recomendaciones
    fun updateRecommendations(newRecommendations: List<String>) {
        recommendations = newRecommendations
        notifyDataSetChanged()
    }
}
