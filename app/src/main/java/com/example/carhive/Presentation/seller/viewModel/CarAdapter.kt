package com.example.carhive.Presentation.seller.viewModel

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.carhive.Domain.model.Car
import com.example.carhive.databinding.ItemSellerCarBinding

class CarAdapter : ListAdapter<Car, CarAdapter.CarViewHolder>(CarDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarViewHolder {
        val binding = ItemSellerCarBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CarViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CarViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class CarViewHolder(private val binding: ItemSellerCarBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(car: Car) {
            binding.textViewModelo.text = car.modelo
            binding.textViewColor.text = car.color
            binding.textViewPrice.text = car.price
            // Agrega más bindings según sea necesario para mostrar las propiedades del coche
            // Por ejemplo, si tienes imágenes:
            // binding.imageViewCar.load(car.imageUrls.firstOrNull()) // O cualquier librería de carga de imágenes
        }
    }

    class CarDiffCallback : DiffUtil.ItemCallback<Car>() {
        override fun areItemsTheSame(oldItem: Car, newItem: Car): Boolean {
            return oldItem.id == newItem.id // Asegúrate de tener un campo ID en tu modelo Car
        }

        override fun areContentsTheSame(oldItem: Car, newItem: Car): Boolean {
            return oldItem == newItem
        }
    }
}
