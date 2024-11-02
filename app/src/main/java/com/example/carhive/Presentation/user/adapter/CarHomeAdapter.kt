package com.example.carhive.Presentation.user.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.carhive.Data.model.CarEntity
import com.example.carhive.R

// Adapter for displaying and managing a list of cars in a RecyclerView with favorite and detail options
class CarHomeAdapter(
    private var carList: List<CarEntity>, // List of cars to display
    private val onFavoriteChecked: (CarEntity, Boolean) -> Unit, // Callback for favorite checkbox action
    private val isCarFavorite: (String, (Boolean) -> Unit) -> Unit, // Callback to check if the car is marked as favorite
    private val onCarClick: (CarEntity) -> Unit // Callback for clicking on the car item
) : RecyclerView.Adapter<CarHomeAdapter.CarViewHolder>() {

    // Inflates the layout for each item and creates a ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_car, parent, false)
        return CarViewHolder(view)
    }

    // Binds car data to the ViewHolder at the specified position
    override fun onBindViewHolder(holder: CarViewHolder, position: Int) {
        val car = carList[position]
        holder.bind(car) // Pass car data to bind method
    }

    // Returns the total count of cars in the list
    override fun getItemCount(): Int = carList.size

    // Updates the list of cars and notifies the RecyclerView to refresh
    fun updateCars(newCars: List<CarEntity>) {
        carList = newCars
        notifyDataSetChanged() // Refreshes the entire list
    }

    // ViewHolder class to represent the UI elements for each car item
    inner class CarViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val carModel: TextView = view.findViewById(R.id.carModel) // TextView for car model
        private val carBrand: TextView = view.findViewById(R.id.carBrand) // TextView for car brand
        private val carPrice: TextView = view.findViewById(R.id.carPrice) // TextView for car price
        private val carImage: ImageView = view.findViewById(R.id.carImage) // ImageView for car image
        private val favoriteCheckBox: CheckBox = view.findViewById(R.id.favoriteCheckBox) // Checkbox to mark car as favorite
        private val btnMoreInfo: TextView = view.findViewById(R.id.btnMoreInfo) // Button for more info on the car

        // Binds data and sets up UI and event listeners for each car item
        fun bind(car: CarEntity) {
            carModel.text = car.modelo
            carBrand.text = car.brand
            carPrice.text = itemView.context.getString(R.string.car_price, car.price)


            // Assign car image or set a placeholder if no image is available
            val imageUrl = car.imageUrls?.firstOrNull()
            if (imageUrl != null) {
                Glide.with(itemView.context)
                    .load(imageUrl)
                    .into(carImage)
            } else {
                carImage.setImageResource(R.drawable.ic_img) // Placeholder image
            }

            // Check if the car is marked as favorite in real-time
            isCarFavorite(car.id) { isFavorite ->
                favoriteCheckBox.isChecked = isFavorite
            }

            // Listener for favorite checkbox state change
            favoriteCheckBox.setOnCheckedChangeListener { _, isChecked ->
                onFavoriteChecked(car, isChecked) // Trigger favorite toggle
            }

            // Click listener for car item to open car details
            itemView.setOnClickListener {
                onCarClick(car)
            }

            // Click listener for "More Info" button to open car details
            btnMoreInfo.setOnClickListener {
                onCarClick(car)
            }
        }
    }
}
