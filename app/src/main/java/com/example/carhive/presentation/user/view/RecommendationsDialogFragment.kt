package com.example.carhive.presentation.user.view

import android.app.Dialog
import android.os.Bundle
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import com.example.carhive.R
import com.example.carhive.databinding.FragmentRecommendationsDialogBinding
import com.example.carhive.presentation.user.adapter.RecommendationsAdapter

class RecommendationsDialogFragmentUser : DialogFragment() {

    private var _binding: FragmentRecommendationsDialogBinding? = null
    private val binding get() = _binding!!

    // Lista de recomendaciones
    private val recommendations = listOf(
        "Meet in public and safe places.",
        "Verify the legal documents of the vehicle before purchasing.",
        "Avoid making full payments before seeing the vehicle in person.",
        "Request a vehicle inspection report before purchasing.",
        "Confirm the seller's identity and ownership of the vehicle."
    )

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = FragmentRecommendationsDialogBinding.inflate(layoutInflater)

        val dialog = Dialog(requireContext())
        dialog.setContentView(binding.root)

        // Cambiar el título a "Recommendations for Buyers"
        binding.tvRecommendationsTitle.text = "Recommendations for Buyers"

        // Configurar RecyclerView con LayoutManager Horizontal
        val layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.recyclerViewRecommendations.layoutManager = layoutManager
        binding.recyclerViewRecommendations.adapter = RecommendationsAdapter(recommendations)

        // Añadir SnapHelper para que cada item se centre
        val snapHelper = PagerSnapHelper()
        snapHelper.attachToRecyclerView(binding.recyclerViewRecommendations)

        // Configurar botón de cerrar
        binding.btnClose.setOnClickListener {
            dismiss()
        }

        return dialog
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            resources.getDimensionPixelSize(R.dimen.dialog_width),
            resources.getDimensionPixelSize(R.dimen.dialog_height)
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
