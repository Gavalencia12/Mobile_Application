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
class CarAdapter(
    private var cars: List<CarEntity>, // List of cars to display
    private val activity: FragmentActivity, // Context to show dialogs
    private val viewModel: CrudViewModel // ViewModel to manage car operations
) : RecyclerView.Adapter<CarAdapter.CarViewHolder>() {

    // ViewHolder class to represent each car item in the RecyclerView
    class CarViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val editButton: Button = itemView.findViewById(R.id.editButton)
        val deleteButton: Button = itemView.findViewById(R.id.deleteButton)
        val viewMoreButton: Button = itemView.findViewById(R.id.viewMoreButton)
        val modeloTextView: TextView = itemView.findViewById(R.id.carModelTextView)
        val priceTextView: TextView = itemView.findViewById(R.id.carPriceTextView)
        val speedTextView: TextView = itemView.findViewById(R.id.carSpeedTextView)
        val carImageView: ImageView = itemView.findViewById(R.id.carImageView)
        val soldIcon: ImageView = itemView.findViewById(R.id.soldIcon)
    }

    // Inflate the view for each car item in the RecyclerView
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_seller_car, parent, false)
        return CarViewHolder(itemView)
    }

    // Bind the car data to the views in each item
    override fun onBindViewHolder(holder: CarViewHolder, position: Int) {
        val car = cars[position]

        // Set the car model text
        holder.modeloTextView.text = car.modelo
        // Format the price and set the text
        holder.priceTextView.text = "$${String.format("%.2f", car.price.toDouble())}"
        // Set the speed text
        holder.speedTextView.text = "${car.speed} km/h"

        // Load the car image using Glide
        val imageUrl = car.imageUrls?.firstOrNull()
        if (imageUrl != null) {
            Glide.with(holder.itemView.context)
                .load(imageUrl)
                .placeholder(R.drawable.ic_img) // Placeholder while loading
                .error(R.drawable.ic_error) // Error image if load fails
                .into(holder.carImageView)
        } else {
            holder.carImageView.setImageResource(R.drawable.ic_img) // Default image if no URL
        }

        // Update the UI based on whether the car is sold
        updateSoldIcon(holder.soldIcon, car.sold)

        // Listener to toggle the "sold" status of the car when the sold icon is clicked
        holder.soldIcon.setOnClickListener {
            activity.lifecycleScope.launch {
                car.sold = !car.sold // Toggle the sold status
                updateSoldIcon(holder.soldIcon, car.sold) // Update the sold icon

                // Get the current user ID from the ViewModel and update the sold status in the database
                val userId = viewModel.getCurrentUserId()
                viewModel.updateCarSoldStatus(userId, car.id, car.sold)
            }
        }

        // Listener to view more details about the car
        holder.viewMoreButton.setOnClickListener {
            val dialog = CarDetailDialogFragment(car)
            dialog.show(activity.supportFragmentManager, "CarDetailDialog")
        }

        // Listener to delete the car
        holder.deleteButton.setOnClickListener {
            ConfirmDeleteDialogFragment(car.id, viewModel)
                .show(activity.supportFragmentManager, "ConfirmDeleteDialog")
        }

        // Listener to edit the car details
        holder.editButton.setOnClickListener {
            val dialog = EditCarDialogFragment.newInstance(car, viewModel)
            dialog.show(activity.supportFragmentManager, "EditCarDialog")
        }
    }

    // Function to update the "sold" icon based on the car's sold status
    private fun updateSoldIcon(soldIcon: ImageView, isSold: Boolean) {
        if (isSold) {
            // Set the icon to green if the car is sold
            soldIcon.setColorFilter(ContextCompat.getColor(soldIcon.context, R.color.green))
        } else {
            // Set the icon to gray if the car is unsold
            soldIcon.setColorFilter(ContextCompat.getColor(soldIcon.context, R.color.gray))
        }
    }

    // Return the number of items in the car list
    override fun getItemCount() = cars.size

    // Update the car list and refresh the RecyclerView
    fun updateCars(newCars: List<CarEntity>) {
        cars = newCars
        notifyDataSetChanged()
    }
}
