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
import com.google.android.material.tabs.TabLayout

class RecommendationsDialogFragment : DialogFragment() {

    private var _binding: FragmentRecommendationsDialogBinding? = null
    private val binding get() = _binding!!

    // Mapa de categorías a listas de recomendaciones
    private val recommendationsMap = mapOf(
        "Safety" to listOf(
            "Do not meet in private or dangerous places.",
            "Verify the buyer's identity before any meeting.",
            "Avoid sharing sensitive personal information.",
            "Conduct the transaction in public and safe places.",
            "Request secure payment before delivering the vehicle."
        ),
        "Financial" to listOf(
            "Compare prices from different sellers.",
            "Check the vehicle's history report.",
            "Understand the total cost of ownership.",
            "Negotiate the price effectively.",
            "Be aware of additional fees and taxes."
        )
    )

    private lateinit var recommendationsAdapter: RecommendationsAdapter

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = FragmentRecommendationsDialogBinding.inflate(layoutInflater)

        val dialog = Dialog(requireContext(), R.style.DialogTheme_Transparent)
        dialog.setContentView(binding.root)

        // Configurar el título
        binding.tvRecommendationsTitle.text = "Buyer Recommendations"

        // Configurar RecyclerView con LayoutManager Horizontal
        val layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.recyclerViewRecommendations.layoutManager = layoutManager

        // Inicializar adaptador con la primera categoría
        val initialCategory = "Safety"
        recommendationsAdapter = RecommendationsAdapter(recommendationsMap[initialCategory] ?: emptyList())
        binding.recyclerViewRecommendations.adapter = recommendationsAdapter

        // Añadir SnapHelper para que cada item se centre
        val snapHelper = PagerSnapHelper()
        snapHelper.attachToRecyclerView(binding.recyclerViewRecommendations)

        // Configurar TabLayout
        setupTabLayout()

        // Configurar botón de cerrar
        binding.btnClose.setOnClickListener {
            dismiss()
        }

        return dialog
    }

    private fun setupTabLayout() {
        val tabLayout: TabLayout = binding.tabLayoutRecommendations

        // Añadir pestañas dinámicamente según las claves del mapa
        recommendationsMap.keys.forEach { category ->
            tabLayout.addTab(tabLayout.newTab().setText(category))
        }

        // Seleccionar la primera pestaña por defecto
        if (tabLayout.tabCount > 0) {
            tabLayout.getTabAt(0)?.select()
        }

        // Escuchar cambios de selección de pestañas
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.let {
                    val category = it.text.toString()
                    updateRecommendations(category)
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                // No se requiere acción
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
                // No se requiere acción
            }
        })
    }

    private fun updateRecommendations(category: String) {
        val newRecommendations = recommendationsMap[category] ?: emptyList()
        recommendationsAdapter.updateRecommendations(newRecommendations)
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.85).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
