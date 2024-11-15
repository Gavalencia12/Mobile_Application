package com.example.carhive.presentation.admin.view.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.carhive.data.model.CarEntity
import com.example.carhive.R
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DatabaseReference


class CarAdapter(
    private var carList: List<CarEntity>,
    private val onCarClick: (CarEntity) -> Unit
) : RecyclerView.Adapter<CarAdapter.CarViewHolder>() {

    private val database: DatabaseReference = FirebaseDatabase.getInstance().getReference("Users")
    fun updateList(newList: List<CarEntity>) {
        carList = newList
        notifyDataSetChanged()
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_admin_car, parent, false)
        return CarViewHolder(view)
    }

    override fun onBindViewHolder(holder: CarViewHolder, position: Int) {
        val car = carList[position]
        holder.bind(car)
    }

    override fun getItemCount(): Int = carList.size

    inner class CarViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val carImageView: ImageView = itemView.findViewById(R.id.car_image)
        private val modelTextView: TextView = itemView.findViewById(R.id.car_model)
        private val brandTextView: TextView = itemView.findViewById(R.id.car_brand)
        private val priceTextView: TextView = itemView.findViewById(R.id.car_price)
        private val firstNameTextView: TextView = itemView.findViewById(R.id.car_fistname)


        fun bind(car: CarEntity) {
            modelTextView.text = car.modelo
            brandTextView.text = car.brand
            priceTextView.text = car.price
            loadOwnerName(car.ownerId)

            val imageUrl = car.imageUrls?.firstOrNull()
            if (imageUrl != null) {
                Glide.with(itemView.context)
                    .load(imageUrl)
                    .into(carImageView)
            }

            itemView.setOnClickListener {
                onCarClick(car)
            }
        }
        private fun loadOwnerName(ownerId: String) {
            database.child(ownerId).get().addOnSuccessListener { dataSnapshot ->
                val firstName = dataSnapshot.child("firstName").getValue(String::class.java)
                firstNameTextView.text = firstName ?: "Unknown"
            }.addOnFailureListener {
                firstNameTextView.text = "Error"
            }
        }
    }
}




