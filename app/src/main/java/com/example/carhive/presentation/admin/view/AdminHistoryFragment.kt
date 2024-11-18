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
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AdminHistoryFragment : Fragment() {

    private var _binding: FragmentAdminHistoryBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AdminHistoryViewModel by viewModels()
    private lateinit var historyAdapter: HistoryAdapter
    private var historyList: List<HistoryEntity> = listOf()
    private var userMap: MutableMap<String, UserEntity?> = mutableMapOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.historyRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Inicializar el adaptador
        historyAdapter = HistoryAdapter(historyList, userMap)
        binding.historyRecyclerView.adapter = historyAdapter

        // Cargar historial
        viewModel.getHistoryData { historyList ->
            if (historyList.isNotEmpty()) {
                this.historyList = historyList
                val userMap = mutableMapOf<String, UserEntity?>()

                // Obtener los datos de los usuarios
                historyList.forEach { historyItem ->
                    val userId = historyItem.userId
                    if (userId != null && !userMap.containsKey(userId)) {
                        viewModel.getUserData(userId) { user ->
                            user?.let {
                                userMap[userId] = it
                                this.userMap = userMap
                                historyAdapter.notifyDataSetChanged()
                            }
                        }
                    }
                }

                // Establecer los datos iniciales en el adaptador
                historyAdapter = HistoryAdapter(historyList, userMap)
                binding.historyRecyclerView.adapter = historyAdapter
            } else {
                Log.d("AdminHistoryFragment", "No hay datos disponibles en el historial.")
            }
        }

        // Configurar el filtro de búsqueda
        binding.searchInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString()
                val filteredList = viewModel.filterHistory(historyList, query)
                historyAdapter = HistoryAdapter(filteredList, userMap)
                binding.historyRecyclerView.adapter = historyAdapter
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        binding.bureturn.setOnClickListener {
            findNavController().navigate(R.id.action_adminHistoryFragment_to_adminHomeFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
