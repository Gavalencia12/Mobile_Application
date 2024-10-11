package com.example.carhive.Presentation.seller.items

import androidx.recyclerview.widget.RecyclerView
import com.example.carhive.Domain.model.Car
import com.example.carhive.databinding.ItemSellerCarBinding

class CarViewHolder(private val binding: ItemSellerCarBinding) : RecyclerView.ViewHolder(binding.root) {
    fun bind(car: Car) {
        binding.textViewModelo.text = car.modelo
        binding.textViewColor.text = car.color
        binding.textViewPrice.text = car.price
        // Agrega el ID si es necesario, por ejemplo, para un log o un botón de acción
        // binding.textViewId.text = car.id // Si tienes un TextView para mostrar el ID
    }
}
