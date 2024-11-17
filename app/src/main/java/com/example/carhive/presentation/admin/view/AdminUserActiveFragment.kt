package com.example.carhive.presentation.admin.view

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.carhive.presentation.admin.viewmodel.AdminUserActiveViewModel
import com.example.carhive.R
import com.example.carhive.databinding.FragmentAdminActivateUserBinding
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import androidx.core.content.ContextCompat


class AdminUserActiveFragment : Fragment() {
    private var _binding: FragmentAdminActivateUserBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AdminUserActiveViewModel by viewModels()
    private var mostrandoVistaDiaria = true

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAdminActivateUserBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.usuarios.observe(viewLifecycleOwner) { usuarios ->
            if (usuarios.isNotEmpty()) {
                mostrarGrafica()
            }
        }

        binding.bureturn.setOnClickListener {
            findNavController().navigate(R.id.action_adminUserActiveFragment_to_adminHomeFragment)
        }

        binding.btToggleView.setOnClickListener {
            mostrandoVistaDiaria = !mostrandoVistaDiaria
            binding.btToggleView.text = if (mostrandoVistaDiaria) {
                "Switch to Monthly View"
            } else {
                "Switch to Daily View"
            }
            mostrarGrafica()
        }
    }

    class DayAxisValueFormatter(private val isDailyView: Boolean) : ValueFormatter() {
        @RequiresApi(Build.VERSION_CODES.O)
        override fun getFormattedValue(value: Float): String {
            return if (isDailyView) {
                when (value.toInt()) {
                    0 -> "Mon"
                    1 -> "Tue"
                    2 -> "Wed"
                    3 -> "Thu"
                    4 -> "Fri"
                    5 -> "Sat"
                    6 -> "Sun"
                    else -> ""
                }
            } else {
                when (value.toInt()) {
                    0 -> "Jan"
                    1 -> "Feb"
                    2 -> "Mar"
                    3 -> "Apr"
                    4 -> "May"
                    5 -> "Jun"
                    6 -> "Jul"
                    7 -> "Aug"
                    8 -> "Sep"
                    9 -> "Oct"
                    10 -> "Nov"
                    11 -> "Dec"
                    else -> ""
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun mostrarGrafica() {
        val (verificados, desverificados) = if (mostrandoVistaDiaria) {
            viewModel.obtenerConteoUsuariosPorDia()
        } else {
            viewModel.obtenerConteoUsuariosPorMes()
        }
        actualizarGrafica(verificados, desverificados)
    }

    private fun actualizarGrafica(verificados: List<Int>, desverificados: List<Int>) {
        val entriesVerificados = verificados.mapIndexed { index, count -> BarEntry(index.toFloat(), count.toFloat()) }
        val entriesDesverificados = desverificados.mapIndexed { index, count -> BarEntry(index.toFloat(), count.toFloat()) }

        val dataSetVerificados = BarDataSet(entriesVerificados, "Verified Users").apply {
            color = ContextCompat.getColor(requireContext(), R.color.blue)
        }

        val dataSetDesverificados = BarDataSet(entriesDesverificados, "Deactivated Users").apply {
            color = ContextCompat.getColor(requireContext(), R.color.blue_pressed)
        }

        val barData = BarData(dataSetVerificados, dataSetDesverificados).apply {
            barWidth = 0.4f
            groupBars(0f, 0.05f, 0.05f)
        }

        binding.chartUsuariosActivos.data = barData
        binding.chartUsuariosActivos.invalidate()

        binding.chartUsuariosActivos.xAxis.valueFormatter = DayAxisValueFormatter(mostrandoVistaDiaria)
        binding.chartUsuariosActivos.xAxis.labelCount = if (mostrandoVistaDiaria) 7 else 12

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}