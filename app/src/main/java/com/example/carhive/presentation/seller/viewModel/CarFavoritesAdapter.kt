package com.example.carhive.presentation.seller.viewModel

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.carhive.data.model.CarEntity
import com.example.carhive.presentation.seller.view.CarDetailDialogFragment
import com.example.carhive.R

// Adapter to display a list of cars with favorite counts in a RecyclerView for the seller
class CarFavoritesAdapter(
    private var cars: List<CarEntity>, // List of cars to display
    private var favoriteCounts: Map<String, Int>, // Map of favorite counts by car ID
    private val activity: FragmentActivity, // Context to show dialogs
    private val viewModel: CrudViewModel // ViewModel to manage car operations
) : RecyclerView.Adapter<CarFavoritesAdapter.CarViewHolder>() {

    // ViewHolder class to represent each car item in the RecyclerView
    class CarViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val viewMoreButton: Button = itemView.findViewById(R.id.viewMoreButton)
        val modeloTextView: TextView = itemView.findViewById(R.id.carModelTextView)
        val priceTextView: TextView = itemView.findViewById(R.id.carPriceTextView)
        val carImageView: ImageView = itemView.findViewById(R.id.carImageView)
        val favoriteCountTextView: TextView = itemView.findViewById(R.id.carFavoritesTextView) // TextView for displaying favorite count
    }

    // Inflate the layout for each car item in the RecyclerView
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_seller_car_favorites, parent, false)
        return CarViewHolder(itemView)
    }

    // Bind the car data and favorite count to the views in each item
    override fun onBindViewHolder(holder: CarViewHolder, position: Int) {
        val car = cars[position]

        // Set car model and price in the ViewHolder's TextViews
        holder.modeloTextView.text = car.modelo
        holder.priceTextView.text = activity.getString(R.string.car_price, String.format("%.2f", car.price.toDouble()))

        // Load the car image using Glide, or set a default image if no URL is provided
        val imageUrl = car.imageUrls?.firstOrNull()
        Glide.with(holder.itemView.context)
            .load(imageUrl ?: R.drawable.ic_img)
            .placeholder(R.drawable.ic_img) // Placeholder image while loading
            .error(R.drawable.ic_error) // Image displayed if loading fails
            .into(holder.carImageView)

        // Retrieve and display the favorite count for the current car using string resource
        val favoriteCount = favoriteCounts[car.id] ?: 0 // Default to 0 if no count available
        holder.favoriteCountTextView.text = holder.itemView.context.getString(R.string.car_favorites_count, favoriteCount)

        // Listener to view more details about the car when "View More" is clicked
        holder.viewMoreButton.setOnClickListener {
            CarDetailDialogFragment(car).show(activity.supportFragmentManager, activity.getString(R.string.car_detail_dialog_title))
        }
    }

    // Return the number of items in the car list
    override fun getItemCount() = cars.size

    // Update the car list and favorite counts, then refresh the RecyclerView
    fun updateCars(newCars: List<CarEntity>, newFavoriteCounts: Map<String, Int>) {
        cars = newCars
        favoriteCounts = newFavoriteCounts
        notifyDataSetChanged()
    }
}
