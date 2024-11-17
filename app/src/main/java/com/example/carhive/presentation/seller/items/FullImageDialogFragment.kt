package com.example.carhive.presentation.seller.view

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
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
        val dialog = super.onCreateDialog(savedInstanceState) // Create the dialog
        dialog.setCanceledOnTouchOutside(true) // Allow dialog to be dismissed when touched outside
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        return dialog // Return the created dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for the dialog
        val view = inflater.inflate(R.layout.dialog_full_image, container, false)

        view.setPadding(50, 50, 50, 50)

        val imageView: ImageView = view.findViewById(R.id.fullImageView) // Reference to the ImageView

        // Load the image using Glide
        Glide.with(requireContext()).load(imageUrl).into(imageView)

        // Dismiss the dialog when the image is clicked
        imageView.setOnClickListener {
            dismiss() // Close the dialog
        }

        return view // Return the inflated view
    }

    override fun onStart() {
        super.onStart()
        // Configure the size of the DialogFragment to occupy the full screen
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT, // Match parent width
            WindowManager.LayoutParams.MATCH_PARENT // Match parent height
        )
    }
}
