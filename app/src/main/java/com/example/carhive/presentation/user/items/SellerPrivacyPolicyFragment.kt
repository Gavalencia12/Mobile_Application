package com.example.carhive.Presentation.user.items

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.carhive.MainActivity
import com.example.carhive.R
import com.example.carhive.databinding.FragmentSellerProfilePrivacyPolicyBinding

class SellerPrivacyPolicyFragment : Fragment() {
    private var _binding: FragmentSellerProfilePrivacyPolicyBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSellerProfilePrivacyPolicyBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.close.setOnClickListener {
            findNavController().popBackStack()
            (activity as MainActivity).bottomNavigationViewSeller.selectedItemId = R.id.profile
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
