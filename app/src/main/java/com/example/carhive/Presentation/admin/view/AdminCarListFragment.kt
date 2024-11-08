package com.example.carhive.Presentation.admin.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.carhive.Presentation.admin.viewmodel.AdminCarListViewModel
import com.example.carhive.Presentation.admin.view.Adapters.CarAdapter
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
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}