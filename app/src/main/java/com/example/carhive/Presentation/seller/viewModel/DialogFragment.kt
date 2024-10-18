package com.example.carhive.Presentation.seller.view

import android.app.Dialog
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
        val builder = AlertDialog.Builder(requireContext())
        val view = requireActivity().layoutInflater.inflate(R.layout.dialog_car_detail, null)

        // Referencias a los elementos del diálogo
        val carModelTextView: TextView = view.findViewById(R.id.carModelDetailTextView)
        val carColorTextView: TextView = view.findViewById(R.id.carColorDetailTextView)
        val carSpeedTextView: TextView = view.findViewById(R.id.carSpeedDetailTextView)
        val carPriceTextView: TextView = view.findViewById(R.id.carPriceDetailTextView)
        val carAddOnTextView: TextView = view.findViewById(R.id.carAddOnDetailTextView)
        val carDescriptionTextView: TextView = view.findViewById(R.id.carDescriptionDetailTextView)
        val closeButton: Button = view.findViewById(R.id.closeButton)
        val imageCountTextView: TextView = view.findViewById(R.id.imageCountTextView)

        // Configurar los datos en el diálogo
        carModelTextView.text = "Model: ${car.modelo}"
        carColorTextView.text = "Color: ${car.color}"
        carSpeedTextView.text = "Speed: ${car.speed} km/h"
        carPriceTextView.text = "Price: $${car.price}"
        carAddOnTextView.text = "Add On: ${car.addOn}"
        carDescriptionTextView.text = "Description: ${car.description}"

        // Actualiza el contador de imágenes
        imageCountTextView.text = "Image 1 of ${car.imageUrls?.size ?: 0}"

        // Configura el ViewPager
        val viewPager: ViewPager = view.findViewById(R.id.carImagesViewPager)
        val imageAdapter = CarImageAdapter(car.imageUrls ?: emptyList())
        viewPager.adapter = imageAdapter

        // Actualiza el contador de imágenes
        imageCountTextView.text = "1/${car.imageUrls?.size ?: 0}"

        // Listener para actualizar el conteo de imágenes al deslizar
        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

            override fun onPageSelected(position: Int) {
                imageCountTextView.text = "${position + 1}/${car.imageUrls?.size ?: 0}"
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })

        // Configura el botón de cerrar
        closeButton.setOnClickListener {
            dismiss()
        }

        // Set the custom view for the dialog
        builder.setView(view)
        builder.setTitle("Car Details")
        return builder.create()
    }
}
