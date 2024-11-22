package com.example.carhive.Presentation.user.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.carhive.R

class BrandAdapter(
    private var brands: MutableList<String>,
    private val selectedBrands: MutableSet<String>,
    private val onBrandSelectionChanged: (Set<String>) -> Unit // Callback para cambios de selección
) : RecyclerView.Adapter<BrandAdapter.BrandViewHolder>() {

    // Inflar y crear el ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BrandViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_brand, parent, false)
        return BrandViewHolder(view)
    }

    // Enlazar los datos a la vista
    override fun onBindViewHolder(holder: BrandViewHolder, position: Int) {
        val brand = brands[position]
        holder.bind(brand)
    }

    // Contar los elementos en la lista
    override fun getItemCount(): Int = brands.size

    // Actualiza la lista de marcas
    fun updateBrands(newBrands: List<String>) {
        brands.clear() // Limpiar la lista de marcas actual
        brands.addAll(newBrands) // Agregar las nuevas marcas
        notifyDataSetChanged() // Notificar que los datos cambiaron
    }

    // ViewHolder para manejar cada elemento
    inner class BrandViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val brandName: TextView = view.findViewById(R.id.brand_name)

        fun bind(brand: String) {
            brandName.text = brand
            updateViewState(brand)

            itemView.setOnClickListener {
                toggleSelection(brand)
                onBrandSelectionChanged(selectedBrands)
            }
        }

        private fun updateViewState(brand: String) {
            itemView.setBackgroundColor(if (selectedBrands.contains(brand)) Color.LTGRAY else Color.TRANSPARENT)
            brandName.setTextColor(if (selectedBrands.contains(brand)) Color.BLUE else Color.BLACK)
        }

        private fun toggleSelection(brand: String) {
            if (selectedBrands.contains(brand)) {
                selectedBrands.remove(brand)
            } else {
                selectedBrands.add(brand)
            }
            notifyItemChanged(adapterPosition)
        }
    }
}
