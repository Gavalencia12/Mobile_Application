package com.example.carhive.presentation.user.items

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.carhive.MainActivity
import com.example.carhive.R
import com.example.carhive.databinding.FragmentUserProfileTermsBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TermsUserFragment: Fragment() {

    private var _binding: FragmentUserProfileTermsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserProfileTermsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.closeButton.setOnClickListener{
            findNavController().navigate(R.id.action_userProfileFragment_to_sellerProfileFragment)
            (activity as MainActivity).bottomNavigationViewUser.selectedItemId = R.id.profile
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}