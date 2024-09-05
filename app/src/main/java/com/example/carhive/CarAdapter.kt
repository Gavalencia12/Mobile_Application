package com.example.carhive

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.carhive.models.Car

class CarAdapter(
    private val context: Context,
    private val cars: List<Car>,
    private val onUpdateClick: (Car) -> Unit,
    private val onDeleteClick: (Car) -> Unit
) : RecyclerView.Adapter<CarAdapter.CarViewHolder>() {

    inner class CarViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvCarName: TextView = view.findViewById(R.id.tvCarName)
        val btnUpdate: Button = view.findViewById(R.id.btnUpdate)
        val btnDelete: Button = view.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_car, parent, false)
        return CarViewHolder(view)
    }

    override fun onBindViewHolder(holder: CarViewHolder, position: Int) {
        val car = cars[position]
        holder.tvCarName.text = car.name

        holder.btnUpdate.setOnClickListener {
            onUpdateClick(car)
        }

        holder.btnDelete.setOnClickListener {
            onDeleteClick(car)
        }
    }

    override fun getItemCount() = cars.size
}
