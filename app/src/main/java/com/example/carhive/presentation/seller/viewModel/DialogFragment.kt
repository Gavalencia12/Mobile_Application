package com.example.carhive.presentation.seller.view

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.viewpager.widget.ViewPager
import com.example.carhive.data.model.CarEntity
import com.example.carhive.presentation.seller.items.CarImageAdapter
import com.example.carhive.R

class CarDetailDialogFragment(private val car: CarEntity) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Initialize AlertDialog Builder
        val builder = AlertDialog.Builder(requireContext())

        // Inflate custom layout for the dialog
        val view = requireActivity().layoutInflater.inflate(R.layout.dialog_car_detail, null)

        // Get references to dialog UI elements
        val carModelTextView: TextView = view.findViewById(R.id.carModelDetailTextView)
        val carColorTextView: TextView = view.findViewById(R.id.carColorDetailTextView)
        val carTransmissionTextView: TextView = view.findViewById(R.id.carTransmissionDetailTextView)
        val carFuelTypeTextView: TextView = view.findViewById(R.id.carFuelTypeDetailTextView)
        val carDoorsTextView: TextView = view.findViewById(R.id.carDoorsDetailTextView)
        val carEngineCapacityTextView: TextView = view.findViewById(R.id.carEngineCapacityDetailTextView)
        val carLocationTextView: TextView = view.findViewById(R.id.carLocationDetailTextView)
        val carConditionTextView: TextView = view.findViewById(R.id.carConditionDetailTextView)
        val carVinTextView: TextView = view.findViewById(R.id.carVinDetailTextView)
        val carPreviousOwnersTextView: TextView = view.findViewById(R.id.carPreviousOwnersDetailTextView)
        val carPriceTextView: TextView = view.findViewById(R.id.carPriceDetailTextView)
        val carDescriptionTextView: TextView = view.findViewById(R.id.carDescriptionDetailTextView)
        val closeButton: Button = view.findViewById(R.id.closeButton)
        val imageCountTextView: TextView = view.findViewById(R.id.imageCountTextView)

        // Set car details into the respective TextViews, replacing text with string resources
        carModelTextView.text = getString(R.string.car_model, car.modelo)
        carColorTextView.text = getString(R.string.car_color, car.color)
        carTransmissionTextView.text = getString(R.string.car_transmission, car.transmission)
        carFuelTypeTextView.text = getString(R.string.car_fuel_type, car.fuelType)
        carDoorsTextView.text = getString(R.string.car_doors, car.doors)
        carEngineCapacityTextView.text = getString(R.string.car_engine_capacity, car.engineCapacity)
        carLocationTextView.text = getString(R.string.car_location, car.location)
        carConditionTextView.text = getString(R.string.car_condition, car.condition)
        carVinTextView.text = getString(R.string.car_vin, car.vin)
        carPreviousOwnersTextView.text = getString(R.string.car_previous_owners, car.previousOwners)
        carPriceTextView.text = getString(R.string.car_price, car.price)
        carDescriptionTextView.text = getString(R.string.car_description, car.description)

        // Set initial image count
        imageCountTextView.text = getString(R.string.image_count, 1, car.imageUrls?.size ?: 0)

        // Set up ViewPager for car images
        val viewPager: ViewPager = view.findViewById(R.id.carImagesViewPager)
        val imageAdapter = CarImageAdapter(car.imageUrls ?: emptyList())
        viewPager.adapter = imageAdapter

        // Listener to update image count as the user scrolls through images
        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

            override fun onPageSelected(position: Int) {
                // Update image count text
                imageCountTextView.text = getString(R.string.image_count, position + 1, car.imageUrls?.size ?: 0)
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })

        // Configure the close button to dismiss the dialog
        closeButton.setOnClickListener {
            dismiss()
        }

        // Set the custom view for the dialog
        builder.setView(view)
        builder.setTitle(getString(R.string.car_details_title))
        val dialog = builder.create()

        // Create a rounded background programmatically
        val roundedBackground = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 50f // Rounded corners radius
            setColor(Color.WHITE) // White background color
        }

        // Apply rounded background to the dialog window
        dialog.window?.setBackgroundDrawable(roundedBackground)

        return dialog
    }
}
