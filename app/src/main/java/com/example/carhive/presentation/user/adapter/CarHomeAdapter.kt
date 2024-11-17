package com.example.carhive.Presentation.user.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.carhive.Presentation.user.viewModel.UserViewModel
import com.example.carhive.data.model.CarEntity
import com.example.carhive.R

class CarHomeAdapter(
    private var carList: List<CarEntity>,
    private val viewModel: UserViewModel,
    private val onFavoriteChecked: (CarEntity, Boolean) -> Unit,
    private val isCarFavorite: (String, (Boolean) -> Unit) -> Unit,
    private val onCarClick: (CarEntity) -> Unit
) : RecyclerView.Adapter<CarHomeAdapter.CarViewHolder>() {

    // Inflate item layout and create ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_car, parent, false)
        return CarViewHolder(view)
    }

    // Bind data to each item in the list
    override fun onBindViewHolder(holder: CarViewHolder, position: Int) {
        val car = carList[position]
        holder.bind(car)
    }

    // Return the total number of items in the list
    override fun getItemCount(): Int = carList.size

    // Update the list of cars and refresh the RecyclerView
    fun updateCars(newCars: List<CarEntity>) {
        carList = newCars
        notifyDataSetChanged()
    }

    // ViewHolder class for each car item in the RecyclerView
    inner class CarViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val carModel: TextView = view.findViewById(R.id.carModel)
        private val carBrand: TextView = view.findViewById(R.id.carBrand)
        private val carPrice: TextView = view.findViewById(R.id.carPrice)
        private val carImage: ImageView = view.findViewById(R.id.carImage)
        private val favoriteCheckBox: CheckBox = view.findViewById(R.id.favoriteCheckBox)
        private val btnMoreInfo: TextView = view.findViewById(R.id.btnMoreInfo)

        // Bind car data to the view elements
        fun bind(car: CarEntity) {
            carModel.text = car.modelo
            carBrand.text = car.brand
            carPrice.text = itemView.context.getString(R.string.car_price, car.price)

            // Load car image with Glide, or display a default image if null
            val imageUrl = car.imageUrls?.firstOrNull()
            if (imageUrl != null) {
                Glide.with(itemView.context).load(imageUrl).into(carImage)
            } else {
                carImage.setImageResource(R.drawable.ic_img)
            }

            // Check if the car is marked as a favorite and set checkbox state
            isCarFavorite(car.id) { isFavorite ->
                favoriteCheckBox.isChecked = isFavorite
            }

            // Handle favorite checkbox toggle event
            favoriteCheckBox.setOnCheckedChangeListener { _, isChecked ->
                onFavoriteChecked(car, isChecked)
            }

            // Handle item click and increment view count
            itemView.setOnClickListener {
                viewModel.handleCarView(car)
                onCarClick(car)
            }

            // Handle "More Info" button click to show car details
            btnMoreInfo.setOnClickListener {
                viewModel.handleCarView(car)
                onCarClick(car)
            }
        }
    }
}
