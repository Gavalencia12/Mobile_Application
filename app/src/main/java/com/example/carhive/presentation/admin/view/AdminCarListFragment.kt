package com.example.carhive.presentation.admin.view

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.carhive.presentation.admin.viewmodel.AdminCarListViewModel
import com.example.carhive.presentation.admin.view.Adapters.CarAdapter
import com.example.carhive.databinding.FragmentCarListBinding
import com.example.carhive.R


class AdminCarListFragment : Fragment() {

    private lateinit var viewModel: AdminCarListViewModel
    private lateinit var carAdapter: CarAdapter
    private var _binding: FragmentCarListBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCarListBinding.inflate(inflater, container, false)
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this).get(AdminCarListViewModel::class.java)

        viewModel.carList.observe(viewLifecycleOwner, Observer { carList ->
            carAdapter = CarAdapter(carList) { car ->
                val dialogFragment = CarDetailDialogFragment.newInstance(car)
                dialogFragment.show(childFragmentManager, "CarDetailDialog")
            }
            binding.recyclerView.adapter = carAdapter
        })

        viewModel.getCars()

        binding.bureturn.setOnClickListener {
            findNavController().navigate(R.id.action_adminCarListFragment_to_adminHomeFragment)
        }

        binding.searchInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                filterCars(s.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }
    private fun filterCars(query: String) {
        val filteredList = viewModel.carList.value?.filter { car ->
            car.modelo.contains(query, ignoreCase = true) ||
                    car.brand.contains(query, ignoreCase = true)
        } ?: emptyList()

        carAdapter.updateList(filteredList)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
