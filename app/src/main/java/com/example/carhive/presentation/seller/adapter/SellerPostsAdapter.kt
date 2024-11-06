package com.example.carhive.presentation.seller.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.carhive.MainActivity
import com.example.carhive.R
import com.example.carhive.data.model.CarEntity

class SellerPostsAdapter(
    private var cars: List<CarEntity>,
    private val onCarClick: (CarEntity) -> Unit
) : RecyclerView.Adapter<SellerPostsAdapter.CarViewHolder>() {

    class CarViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val modeloTextView: TextView = itemView.findViewById(R.id.carModelTextView)
        val priceTextView: TextView = itemView.findViewById(R.id.carPriceTextView)
        val speedTextView: TextView = itemView.findViewById(R.id.carSpeedTextView)
        val carImageView: ImageView = itemView.findViewById(R.id.carImageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_seller_car_chats, parent, false)
        return CarViewHolder(view)
    }

    override fun onBindViewHolder(holder: CarViewHolder, position: Int) {
        val car = cars[position]

        holder.modeloTextView.text = car.modelo
        holder.priceTextView.text = "$${String.format("%.2f", car.price.toDouble())}"
        val imageUrl = car.imageUrls?.firstOrNull()
        if (imageUrl != null) {
            Glide.with(holder.itemView.context)
                .load(imageUrl)
                .placeholder(R.drawable.ic_img)
                .error(R.drawable.ic_error)
                .into(holder.carImageView)
        } else {
            holder.carImageView.setImageResource(R.drawable.ic_img)
        }

        holder.itemView.setOnClickListener {
            onCarClick(car)
        }
    }

    override fun getItemCount() = cars.size

    fun updateCars(newCars: List<CarEntity>) {
        cars = newCars
        notifyDataSetChanged()
    }
}
