package com.example.carhive.presentation.user.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.carhive.data.model.CarEntity
import com.example.carhive.R

// Adapter for displaying and managing a list of favorite cars in a RecyclerView
class CarFavoritesAdapter(
    private var carList: List<CarEntity>, // List of favorite cars to display
    private val onDeleteFavoriteClick: (CarEntity) -> Unit, // Callback for delete action
    private val onCarClick: (CarEntity) -> Unit // Callback for click on car item
) : RecyclerView.Adapter<CarFavoritesAdapter.CarViewHolder>() {

    // ViewHolder class to represent the UI elements for each car item
    class CarViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val carModel: TextView = view.findViewById(R.id.carModel) // TextView for car model
        val carBrand: TextView = view.findViewById(R.id.carBrand) // TextView for car brand
        val carPrice: TextView = view.findViewById(R.id.carPrice) // TextView for car price
        val carImage: ImageView = view.findViewById(R.id.carImage) // ImageView for car image
        val deleteFavoriteButton: ImageButton = view.findViewById(R.id.deleteFavoriteButton) // Button to remove from favorites
    }

    // Inflates the layout for each item and creates a ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_car_favorites, parent, false)
        return CarViewHolder(view)
    }

    // Binds car data to the ViewHolder at the specified position
    override fun onBindViewHolder(holder: CarViewHolder, position: Int) {
        val car = carList[position]
        holder.carModel.text = car.modelo
        holder.carBrand.text = car.brand // Use the brand field
        holder.carPrice.text = holder.itemView.context.getString(R.string.car_price, car.price)

        // Load car image using Glide, or set a placeholder if no image URL is available
        val imageUrl = car.imageUrls?.firstOrNull()
        if (imageUrl != null) {
            Glide.with(holder.itemView.context)
                .load(imageUrl)
                .into(holder.carImage)
        } else {
            holder.carImage.setImageResource(R.drawable.ic_img) // Placeholder image
        }

        // Set up listener for the delete button
        holder.deleteFavoriteButton.setOnClickListener {
            onDeleteFavoriteClick(car) // Trigger delete action
        }

        // Set up listener for clicking on the car item
        holder.itemView.setOnClickListener {
            onCarClick(car) // Trigger item click action
        }
    }

    // Returns the total count of cars in the list
    override fun getItemCount(): Int = carList.size

    // Updates the list of cars and notifies the RecyclerView to refresh
    fun updateCars(newCars: List<CarEntity>) {
        carList = newCars
        notifyDataSetChanged() // Refreshes the entire list
    }
}
