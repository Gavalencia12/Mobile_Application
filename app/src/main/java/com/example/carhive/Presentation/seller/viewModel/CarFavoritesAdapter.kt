package com.example.carhive.Presentation.seller.viewModel

import ConfirmDeleteDialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.carhive.Data.model.CarEntity
import com.example.carhive.Presentation.seller.view.CarDetailDialogFragment
import com.example.carhive.Presentation.seller.view.EditCarDialogFragment
import com.example.carhive.R
import kotlinx.coroutines.launch

// Adapter to display a list of cars in a RecyclerView for the seller
class CarFavoritesAdapter(
    private var cars: List<CarEntity>,
    private var favoriteCounts: Map<String, Int>, // Recibe el mapa de contadores de favoritos
    private val activity: FragmentActivity,
    private val viewModel: CrudViewModel
) : RecyclerView.Adapter<CarFavoritesAdapter.CarViewHolder>() {

    class CarViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val viewMoreButton: Button = itemView.findViewById(R.id.viewMoreButton)
        val modeloTextView: TextView = itemView.findViewById(R.id.carModelTextView)
        val priceTextView: TextView = itemView.findViewById(R.id.carPriceTextView)
        val speedTextView: TextView = itemView.findViewById(R.id.carSpeedTextView)
        val carImageView: ImageView = itemView.findViewById(R.id.carImageView)
        val favoriteCountTextView: TextView = itemView.findViewById(R.id.carFavoritesTextView) // Nuevo TextView para favoritos
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_seller_car_favorites, parent, false)
        return CarViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: CarViewHolder, position: Int) {
        val car = cars[position]

        holder.modeloTextView.text = car.modelo
        holder.priceTextView.text = "$${String.format("%.2f", car.price.toDouble())}"
        holder.speedTextView.text = "${car.speed} km/h"

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

        // Obtener el contador de favoritos para el coche actual y actualizar el TextView
        val favoriteCount = favoriteCounts[car.id] ?: 0
        holder.favoriteCountTextView.text = "Favorites: $favoriteCount"

        // Listener to view more details about the car
        holder.viewMoreButton.setOnClickListener {
            val dialog = CarDetailDialogFragment(car)
            dialog.show(activity.supportFragmentManager, "CarDetailDialog")
        }
    }

    override fun getItemCount() = cars.size

    fun updateCars(newCars: List<CarEntity>, newFavoriteCounts: Map<String, Int>) {
        cars = newCars
        favoriteCounts = newFavoriteCounts
        notifyDataSetChanged()
    }
}
