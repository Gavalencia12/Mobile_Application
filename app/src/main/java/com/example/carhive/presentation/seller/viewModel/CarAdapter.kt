package com.example.carhive.presentation.seller.viewModel

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
import com.example.carhive.data.model.CarEntity
import com.example.carhive.presentation.seller.view.CarDetailDialogFragment
import com.example.carhive.presentation.seller.view.EditCarDialogFragment
import com.example.carhive.R
import kotlinx.coroutines.launch

// RecyclerView Adapter for displaying a list of cars in the seller view
class CarAdapter(
    private var cars: List<CarEntity>,  // List of car items to display
    private val activity: FragmentActivity,  // Context for showing dialogs
    private val viewModel: CrudViewModel  // ViewModel for handling car operations
) : RecyclerView.Adapter<CarAdapter.CarViewHolder>() {

    // ViewHolder to represent each car item in the RecyclerView
    class CarViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val editButton: Button = itemView.findViewById(R.id.editButton)
        val deleteButton: Button = itemView.findViewById(R.id.deleteButton)
        val viewMoreButton: Button = itemView.findViewById(R.id.viewMoreButton)
        val modeloTextView: TextView = itemView.findViewById(R.id.carModelTextView)
        val priceTextView: TextView = itemView.findViewById(R.id.carPriceTextView)
        val brandTextView: TextView = itemView.findViewById(R.id.tv_carBrand)
        val carImageView: ImageView = itemView.findViewById(R.id.carImageView)
        val soldIcon: ImageView = itemView.findViewById(R.id.soldIcon)
    }

    // Inflate each car item layout for the RecyclerView
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_seller_car, parent, false)
        return CarViewHolder(itemView)
    }

    // Bind data to the views for each car item
    override fun onBindViewHolder(holder: CarViewHolder, position: Int) {
        val car = cars[position]

        // Set car details in the ViewHolder's views
        holder.apply {
            editButton.text = activity.getString(R.string.edit_button)
            deleteButton.text = activity.getString(R.string.delete_button)
            modeloTextView.text = car.modelo
            brandTextView.text = car.brand
            priceTextView.text = "$${String.format("%.2f", car.price.toDouble())}"

            // Load the car image using Glide or a placeholder if no URL is available
            val imageUrl = car.imageUrls?.firstOrNull()
            Glide.with(itemView.context)
                .load(imageUrl ?: R.drawable.ic_img)
                .placeholder(R.drawable.ic_img)
                .error(R.drawable.ic_error)
                .into(carImageView)

            // Set the "sold" icon color based on car's sold status
            updateSoldIcon(soldIcon, car.sold)

            // Toggle sold status when clicking on the sold icon
            soldIcon.setOnClickListener {
                toggleSoldStatus(car, soldIcon)
            }

            // Open dialog to view more details about the car
            viewMoreButton.setOnClickListener {
                CarDetailDialogFragment(car).show(activity.supportFragmentManager, activity.getString(R.string.car_detail_dialog))
            }

            // Open delete confirmation dialog
            deleteButton.setOnClickListener {
                ConfirmDeleteDialogFragment(car.id, viewModel).show(activity.supportFragmentManager, activity.getString(R.string.confirm_delete_dialog))
            }

            // Open edit dialog to modify car details
            editButton.setOnClickListener {
                EditCarDialogFragment.newInstance(car, viewModel).show(activity.supportFragmentManager, activity.getString(R.string.edit_car_dialog))
            }
            if (car.approved) {
                holder.itemView.findViewById<TextView>(R.id.approvedText).visibility = View.GONE
                holder.itemView.findViewById<ImageView>(R.id.soldIcon).visibility = View.VISIBLE
                holder.itemView.findViewById<Button>(R.id.deleteButton).visibility = View.VISIBLE
            } else {
                holder.itemView.findViewById<TextView>(R.id.approvedText).visibility = View.VISIBLE
                holder.itemView.findViewById<ImageView>(R.id.soldIcon).visibility = View.GONE
                holder.itemView.findViewById<Button>(R.id.deleteButton).visibility = View.GONE
            }
        }
    }

    // Update the "sold" icon color based on whether the car is sold
    private fun updateSoldIcon(soldIcon: ImageView, isSold: Boolean) {
        val colorRes = if (isSold) R.color.green else R.color.gray
        soldIcon.setColorFilter(ContextCompat.getColor(soldIcon.context, colorRes))
    }

    // Toggle the car's "sold" status and update the icon and database
    private fun toggleSoldStatus(car: CarEntity, soldIcon: ImageView) {
        activity.lifecycleScope.launch {
            car.sold = !car.sold // Toggle status
            updateSoldIcon(soldIcon, car.sold) // Update icon
            viewModel.updateCarSoldStatus(viewModel.getCurrentUserId(), car.id, car.sold)
        }
    }

    // Return the total number of car items in the adapter
    override fun getItemCount() = cars.size

    // Refresh the list of cars and notify the RecyclerView of data changes
    fun updateCars(newCars: List<CarEntity>) {
        cars = newCars
        notifyDataSetChanged()
    }
}
