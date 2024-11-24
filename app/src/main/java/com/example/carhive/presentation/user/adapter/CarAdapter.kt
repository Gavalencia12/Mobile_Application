package com.example.carhive.presentation.user.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.carhive.R
import com.example.carhive.data.model.CarEntity
import com.example.carhive.databinding.ItemCarSellerBinding

class CarAdapter(
    private val cars: List<CarEntity>,
    private val onClick: (CarEntity) -> Unit
) : RecyclerView.Adapter<CarAdapter.CarViewHolder>() {

    inner class CarViewHolder(private val binding: ItemCarSellerBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(car: CarEntity) {
            binding.carPrice.text = "$${car.price}"

            if (!car.imageUrls.isNullOrEmpty()) {
                Glide.with(binding.carImage.context)
                    .load(car.imageUrls!![0])
                    .error(R.drawable.ic_error)
                    .into(binding.carImage)
            }

            binding.root.setOnClickListener { onClick(car) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarViewHolder {
        val binding = ItemCarSellerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CarViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CarViewHolder, position: Int) {
        holder.bind(cars[position])
    }

    override fun getItemCount(): Int = cars.size
}
