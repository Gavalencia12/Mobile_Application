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
    private var cars: List<CarEntity>,
    private val activity: FragmentActivity,
    private val viewModel: CrudViewModel
) : RecyclerView.Adapter<CarAdapter.CarViewHolder>() {

    class CarViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val editButton: Button = itemView.findViewById(R.id.editButton)
        val deleteButton: Button = itemView.findViewById(R.id.deleteButton)
        val viewMoreButton: Button = itemView.findViewById(R.id.viewMoreButton)

        val modeloTextView: TextView = itemView.findViewById(R.id.carModelTextView)
        val colorTextView: TextView = itemView.findViewById(R.id.carColorTextView)
        val priceTextView: TextView = itemView.findViewById(R.id.carPriceTextView)
        val speedTextView: TextView = itemView.findViewById(R.id.carSpeedTextView)
        val carImageView: ImageView = itemView.findViewById(R.id.carImageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_seller_car, parent, false)
        return CarViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: CarViewHolder, position: Int) {
        val car = cars[position]
        holder.modeloTextView.text = car.modelo
        holder.colorTextView.text = "Color: ${car.color}  "
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

        holder.viewMoreButton.setOnClickListener {
            val dialog = CarDetailDialogFragment(car)
            dialog.show(activity.supportFragmentManager, "CarDetailDialog")
        }

        holder.deleteButton.setOnClickListener {
            ConfirmDeleteDialogFragment(car.id, viewModel).show(activity.supportFragmentManager, "ConfirmDeleteDialog")
        }

        // Add edit button functionality
        holder.editButton.setOnClickListener {
            val dialog = EditCarDialogFragment.newInstance(car, viewModel)
            dialog.show(activity.supportFragmentManager, "EditCarDialog")
        }
    }

    override fun getItemCount() = cars.size

    fun updateCars(newCars: List<CarEntity>) {
        cars = newCars
        notifyDataSetChanged()
    }
}