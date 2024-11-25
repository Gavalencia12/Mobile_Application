package com.example.carhive.presentation.admin.view

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.carhive.data.model.HistoryEntity
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.carhive.R
import com.example.carhive.databinding.FragmentAdminHistoryBinding
import com.example.carhive.presentation.admin.viewModel.AdminHistoryViewModel
import com.example.carhive.data.model.UserEntity
import com.example.carhive.data.model.UserReport
import com.example.carhive.databinding.FragmentAdminReportsBinding
import com.example.carhive.presentation.admin.view.Adapters.UserReportDetailsDialogFragment
import com.example.carhive.presentation.admin.view.Adapters.UserReportsAdapter
import com.example.carhive.presentation.admin.viewModel.AdminReportsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AdminReportsFragment : Fragment() {

    private var _binding: FragmentAdminReportsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AdminReportsViewModel by viewModels()
    private lateinit var adapter: UserReportsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminReportsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Configurar botón de regreso
        binding.bureturn.setOnClickListener {
            findNavController().navigate(R.id.action_adminReportsFragment_to_adminHomeFragment)
        }

        // Inicializar el adaptador con la lista de usuarios y el click listener
        adapter = UserReportsAdapter(viewModel.userMap) { userReport ->
            showReportDetails(userReport)
        }

        // Configurar el RecyclerView
        binding.recyclerViewReports.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewReports.adapter = adapter

        // Observar los reportes desde el ViewModel
        viewModel.userReports.observe(viewLifecycleOwner) { userReports ->
            // Ordenar los reportes por timestamp en orden descendente (más recientes primero)
            val sortedReports = userReports.sortedByDescending { it.timestamp }
            adapter.submitList(sortedReports)
        }


        // Cargar los datos iniciales
        viewModel.fetchReports()

        // Configurar la barra de búsqueda para filtrar los reportes
        setupSearchFilter()
    }

    private fun setupSearchFilter() {
        binding.searchInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString().trim()
                filterReports(query)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun filterReports(query: String) {
        val allReports = viewModel.userReports.value.orEmpty()
        val filteredReports = if (query.isEmpty()) {
            allReports
        } else {
            allReports.filter { report ->
                val reporter = viewModel.userMap[report.userId]?.firstName.orEmpty()
                val reportedUser = viewModel.userMap[report.reportedUserId]?.firstName.orEmpty()
                val comment = report.comment.orEmpty()
                reporter.contains(query, ignoreCase = true) ||
                        reportedUser.contains(query, ignoreCase = true) ||
                        comment.contains(query, ignoreCase = true)
            }
        }
        // Ordenar los reportes filtrados en orden descendente
        val sortedReports = filteredReports.sortedByDescending { it.timestamp }
        adapter.submitList(sortedReports)
    }

    private fun showReportDetails(userReport: UserReport) {
        // Abrir el diálogo con los detalles del reporte y mensajes
        val dialog = UserReportDetailsDialogFragment.newInstance(userReport)
        dialog.show(parentFragmentManager, "UserReportDetails")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
