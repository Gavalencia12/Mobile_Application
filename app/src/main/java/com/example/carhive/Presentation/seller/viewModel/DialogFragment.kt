package com.example.carhive.Presentation.seller.view

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.viewpager.widget.ViewPager
import com.example.carhive.Data.model.CarEntity
import com.example.carhive.Presentation.seller.items.CarImageAdapter
import com.example.carhive.R

class CarDetailDialogFragment(private val car: CarEntity) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Create an AlertDialog Builder
        val builder = AlertDialog.Builder(requireContext())

        // Inflate the custom layout for the dialog
        val view = requireActivity().layoutInflater.inflate(R.layout.dialog_car_detail, null)

        // References to dialog elements
        val carModelTextView: TextView = view.findViewById(R.id.carModelDetailTextView)
        val carColorTextView: TextView = view.findViewById(R.id.carColorDetailTextView)
        val carSpeedTextView: TextView = view.findViewById(R.id.carSpeedDetailTextView)
        val carPriceTextView: TextView = view.findViewById(R.id.carPriceDetailTextView)
        val carAddOnTextView: TextView = view.findViewById(R.id.carAddOnDetailTextView)
        val carDescriptionTextView: TextView = view.findViewById(R.id.carDescriptionDetailTextView)
        val closeButton: Button = view.findViewById(R.id.closeButton)
        val imageCountTextView: TextView = view.findViewById(R.id.imageCountTextView)

        // Set the data in the dialog
        carModelTextView.text = "Model: ${car.modelo}"
        carColorTextView.text = "Color: ${car.color}"
        carSpeedTextView.text = "Speed: ${car.speed} km/h"
        carPriceTextView.text = "Price: $${car.price}"
        carAddOnTextView.text = "Add On: ${car.addOn}"
        carDescriptionTextView.text = "Description: ${car.description}"

        // Update the image counter
        imageCountTextView.text = "Image 1 of ${car.imageUrls?.size ?: 0}"

        // Set up the ViewPager for car images
        val viewPager: ViewPager = view.findViewById(R.id.carImagesViewPager)
        val imageAdapter = CarImageAdapter(car.imageUrls ?: emptyList())
        viewPager.adapter = imageAdapter

        // Update the image counter based on the current image
        imageCountTextView.text = "1/${car.imageUrls?.size ?: 0}"

        // Listener to update the image count when swiping through images
        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

            override fun onPageSelected(position: Int) {
                // Update the image count text based on the selected position
                imageCountTextView.text = "${position + 1}/${car.imageUrls?.size ?: 0}"
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })

        // Set the close button listener to dismiss the dialog
        closeButton.setOnClickListener {
            dismiss()
        }

        // Set the custom view for the dialog
        builder.setView(view)
        builder.setTitle("Car Details")
        val dialog = builder.create()

        // Crear el fondo con bordes redondeados programáticamente
        val roundedBackground = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 50f // Ajusta el radio de los bordes redondeados
            setColor(Color.WHITE) // Fondo blanco
        }

        // Aplicar el fondo redondeado al diálogo
        dialog.window?.setBackgroundDrawable(roundedBackground)

        return dialog
    }
}
