package com.example.carhive.Presentation.seller.viewModel

import ConfirmDeleteDialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.carhive.Data.model.CarEntity
import com.example.carhive.Presentation.seller.view.CarDetailDialogFragment
import com.example.carhive.Presentation.seller.view.EditCarDialogFragment
import com.example.carhive.R

class CarAdapter(
    private var cars: List<CarEntity>, // List of cars to be displayed
    private val activity: FragmentActivity, // Activity context for displaying dialogs
    private val viewModel: CrudViewModel // ViewModel to manage car operations
) : RecyclerView.Adapter<CarAdapter.CarViewHolder>() {

    // ViewHolder class to represent each car item in the RecyclerView
    class CarViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val editButton: Button = itemView.findViewById(R.id.editButton)
        val deleteButton: Button = itemView.findViewById(R.id.deleteButton)
        val viewMoreButton: Button = itemView.findViewById(R.id.viewMoreButton)

        val modeloTextView: TextView = itemView.findViewById(R.id.carModelTextView)
        //        val colorTextView: TextView = itemView.findViewById(R.id.carColorTextView)
        val priceTextView: TextView = itemView.findViewById(R.id.carPriceTextView)
        val speedTextView: TextView = itemView.findViewById(R.id.carSpeedTextView)
        val carImageView: ImageView = itemView.findViewById(R.id.carImageView)
    }

    // Inflate the view for each item in the RecyclerView
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_seller_car, parent, false)
        return CarViewHolder(itemView)
    }

    // Bind the data to the views in each item
    override fun onBindViewHolder(holder: CarViewHolder, position: Int) {
        val car = cars[position]
        holder.modeloTextView.text = car.modelo // Set the car model text

        // Show the color to the right of the text (commented out)
        // holder.colorTextView.text = "Color: ${car.color}  " // Adds spaces for separation
        // holder.colorTextView.textAlignment = View.TEXT_ALIGNMENT_VIEW_END // Align text to the right

        // Add the dollar sign to the price and format it to two decimal places
        holder.priceTextView.text = "$${String.format("%.2f", car.price.toDouble())}"

        // Append "km/h" to the speed
        holder.speedTextView.text = "${car.speed} km/h"

        // Load the image using Glide (if there are image URLs)
        val imageUrl = car.imageUrls?.firstOrNull()
        if (imageUrl != null) {
            Glide.with(holder.itemView.context)
                .load(imageUrl)
                .placeholder(R.drawable.ic_img) // Placeholder image while loading
                .error(R.drawable.ic_error) // Error image if loading fails
                .into(holder.carImageView) // Set the loaded image to the ImageView
        } else {
            holder.carImageView.setImageResource(R.drawable.ic_img) // Set default image if no URL is available
        }

        // Set listener for the "View More" button to show car details
        holder.viewMoreButton.setOnClickListener {
            val dialog = CarDetailDialogFragment(car)
            dialog.show(activity.supportFragmentManager, "CarDetailDialog")
        }

        // Set listener for the "Delete" button to confirm deletion
        holder.deleteButton.setOnClickListener {
            ConfirmDeleteDialogFragment(car.id, viewModel).show(activity.supportFragmentManager, "ConfirmDeleteDialog")
        }

        // Add functionality for the "Edit" button
        holder.editButton.setOnClickListener {
            val dialog = EditCarDialogFragment.newInstance(car, viewModel)
            dialog.show(activity.supportFragmentManager, "EditCarDialog")
        }
    }

    // Return the number of items in the list
    override fun getItemCount() = cars.size

    // Update the list of cars when there are new data
    fun updateCars(newCars: List<CarEntity>) {
        cars = newCars
        notifyDataSetChanged() // Notify the adapter that the data has changed
    }
}
