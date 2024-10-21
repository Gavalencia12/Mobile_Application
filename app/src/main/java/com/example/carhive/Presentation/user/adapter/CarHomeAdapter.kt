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

class CarHomeAdapter(
    private var carList: List<CarEntity>,
    private val onFavoriteChecked: (CarEntity, Boolean) -> Unit,
    private val isCarFavorite: (String, (Boolean) -> Unit) -> Unit, // Callback para verificar el estado de favorito
    private val onCarClick: (CarEntity) -> Unit
) : RecyclerView.Adapter<CarHomeAdapter.CarViewHolder>() {

    class CarViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val carModel: TextView = view.findViewById(R.id.carModel)
        val carMarca: TextView = view.findViewById(R.id.carMarca)
        val carPrice: TextView = view.findViewById(R.id.carPrice)
        val carImage: ImageView = view.findViewById(R.id.carImage)
        val favoriteCheckBox: CheckBox = view.findViewById(R.id.favoriteCheckBox)
        val btnMoreInfo: TextView = view.findViewById(R.id.btnMoreInfo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_car, parent, false)
        return CarViewHolder(view)
    }

    override fun onBindViewHolder(holder: CarViewHolder, position: Int) {
        val car = carList[position]
        holder.carModel.text = car.modelo
        holder.carMarca.text = car.addOn
        holder.carPrice.text = "$ ${car.price}"

        // Asignar la imagen del coche
        val imageUrl = car.imageUrls?.firstOrNull()
        if (imageUrl != null) {
            Glide.with(holder.itemView.context)
                .load(imageUrl)
                .into(holder.carImage)
        } else {
            holder.carImage.setImageResource(R.drawable.ic_img)
        }

        // Consultar en tiempo real si el coche es favorito
        isCarFavorite(car.id) { isFavorite ->
            holder.favoriteCheckBox.isChecked = isFavorite
        }

        // Listener para el CheckBox (cuando cambia el estado de favorito)
        holder.favoriteCheckBox.setOnCheckedChangeListener { _, isChecked ->
            onFavoriteChecked(car, isChecked)
        }

        // Click listener para el coche
        holder.itemView.setOnClickListener {
            onCarClick(car)
        }

        holder.btnMoreInfo.setOnClickListener{
            onCarClick(car)
        }
    }

    override fun getItemCount(): Int = carList.size

    fun updateCars(newCars: List<CarEntity>) {
        carList = newCars
        notifyDataSetChanged()
    }
}
