package com.example.carhive.Presentation.seller.viewModel

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.carhive.Data.model.CarEntity
import com.example.carhive.R

class CarAdapter(private var cars: List<CarEntity>) : RecyclerView.Adapter<CarAdapter.CarViewHolder>() {

    // Clase que representa el ViewHolder para cada item de coche
    class CarViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val editButton: Button = itemView.findViewById(R.id.editButton)
        val deleteButton: Button = itemView.findViewById(R.id.deleteButton)

        val modeloTextView: TextView = itemView.findViewById(R.id.carModelTextView)
        val colorTextView: TextView = itemView.findViewById(R.id.carColorTextView)
        val priceTextView: TextView = itemView.findViewById(R.id.carPriceTextView)
        val speedTextView: TextView = itemView.findViewById(R.id.carSpeedTextView)
        val carImageView: ImageView = itemView.findViewById(R.id.carImageView)
    }

    // Infla la vista para cada item en el RecyclerView
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_seller_car, parent, false)
        return CarViewHolder(itemView)
    }

    // Vincula los datos a las vistas en cada item
    override fun onBindViewHolder(holder: CarViewHolder, position: Int) {
        val car = cars[position]
        holder.modeloTextView.text = car.modelo
        holder.colorTextView.text = car.color
        holder.priceTextView.text = car.price
        holder.speedTextView.text = car.speed

        // Cargar la imagen usando Glide (si hay URLs de imágenes)
        // Solo carga la primera imagen de la lista de URLs de imágenes
        val imageUrl = car.imageUrls?.firstOrNull() // Verifica si hay al menos una URL en la lista
        if (imageUrl != null) {
            Glide.with(holder.itemView.context)
                .load(imageUrl)
                .placeholder(R.drawable.ic_img) // Imagen de placeholder mientras carga
                .error(R.drawable.ic_error) // Imagen si hay error al cargar
                .into(holder.carImageView)
        } else {
            // Si no hay imagen, cargar una imagen por defecto
            holder.carImageView.setImageResource(R.drawable.ic_img)
        }


    }

    // Retorna el número de elementos en la lista
    override fun getItemCount() = cars.size

    // Actualiza la lista de coches cuando hay nuevos datos
    fun updateCars(newCars: List<CarEntity>) {
        cars = newCars
        notifyDataSetChanged()
    }
}
