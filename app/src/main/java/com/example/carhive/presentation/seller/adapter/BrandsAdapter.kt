package com.example.carhive.Presentation.user.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.carhive.R // Import the R class to reference string resources

// Adapter to display and manage selectable car brands in a RecyclerView
class BrandAdapter(
    private var brands: MutableList<String>, // MutableList allows for updating the list of brands
    private val selectedBrands: MutableSet<String>, // Stores selected brands
    private val onBrandSelectionChanged: (Set<String>) -> Unit // Callback to notify selection changes
) : RecyclerView.Adapter<BrandAdapter.BrandViewHolder>() {

    // Inflates the item view for each brand and creates a ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BrandViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_brand, parent, false) // Use a custom layout for better flexibility
        return BrandViewHolder(view)
    }

    // Binds data (brand name) to the ViewHolder at the given position
    override fun onBindViewHolder(holder: BrandViewHolder, position: Int) {
        val brand = brands[position]
        holder.bind(brand) // Pass brand name to the bind method
    }

    // Returns the total count of brands in the list
    override fun getItemCount(): Int = brands.size

    // Updates the list of brands with a new set of brands
    fun updateBrands(newBrands: List<String>) {
        brands.clear() // Clear the current list
        brands.addAll(newBrands) // Add the new list of brands
        notifyDataSetChanged() // Notify RecyclerView to refresh
    }

    // ViewHolder class to represent and manage the UI for each brand item
    inner class BrandViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val brandName: TextView = view.findViewById(R.id.brand_name) // TextView for brand name

        // Binds brand data and sets up click functionality
        fun bind(brand: String) {
            brandName.text = brand // Set the brand name text
            updateViewState(brand) // Apply styling based on selection state

            // Toggle selection when item is clicked
            itemView.setOnClickListener {
                toggleSelection(brand) // Update selection state
                onBrandSelectionChanged(selectedBrands) // Notify the selection change
            }
        }

        // Updates the view's appearance based on whether the brand is selected
        private fun updateViewState(brand: String) {
            itemView.setBackgroundColor(if (selectedBrands.contains(brand)) Color.LTGRAY else Color.TRANSPARENT)
            brandName.setTextColor(if (selectedBrands.contains(brand)) Color.BLUE else Color.BLACK)
        }

        // Toggles selection state for the given brand
        private fun toggleSelection(brand: String) {
            if (selectedBrands.contains(brand)) {
                selectedBrands.remove(brand) // Unselect if already selected
            } else {
                selectedBrands.add(brand) // Select if not already selected
            }
            notifyItemChanged(adapterPosition) // Notify that this item has changed
        }
    }
}
