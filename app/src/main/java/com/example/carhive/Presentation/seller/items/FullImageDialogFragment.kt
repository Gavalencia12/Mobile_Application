package com.example.carhive.Presentation.seller.view

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.example.carhive.R

class FullImageDialogFragment(private val imageUrl: String) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setCanceledOnTouchOutside(true)
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.dialog_full_image, container, false)

        val imageView: ImageView = view.findViewById(R.id.fullImageView)

        // Cargar la imagen con Glide
        Glide.with(requireContext()).load(imageUrl).into(imageView)

        // Cerrar el dialog al hacer clic en la imagen
        imageView.setOnClickListener {
            dismiss()
        }

        return view
    }

    override fun onStart() {
        super.onStart()
        // Configurar el tamaño del DialogFragment para ocupar la pantalla completa
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT
        )
    }
}
