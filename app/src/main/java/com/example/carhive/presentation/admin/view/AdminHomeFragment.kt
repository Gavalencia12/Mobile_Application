package com.example.carhive.presentation.admin.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.carhive.presentation.admin.viewModel.AdminHomeViewModel
import com.example.carhive.R
import com.example.carhive.databinding.FragmentAdminHomeBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AdminHomeFragment : Fragment() {
    private var _binding: FragmentAdminHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AdminHomeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.getUserInfo { firstName, lastName ->
            val welcomeMessage = "Welcome, $firstName!"
            binding.btnProfile.text = welcomeMessage
        }

        binding.signOutButton.setOnClickListener {
            viewModel.onLogicClick()
            findNavController().navigate(R.id.action_userFragment_to_loginFragment)
        }

        binding.viewUsersButton.setOnClickListener {
            findNavController().navigate(R.id.action_adminHomeFragment_to_adminUserListFragment)
        }
        binding.viewBanersButton.setOnClickListener {
            findNavController().navigate(R.id.action_adminHomeFragment_to_adminUserBanFragment)
        }
        binding.viewstadisticsButton.setOnClickListener {
            findNavController().navigate(R.id.action_adminHomeFragment_to_adminUserActiveFragment)
        }
//        binding.btnProfile.setOnClickListener {
//            findNavController().navigate(R.id.action_adminHomeFragment_to_userProfileFragment)
//        }
        binding.btnNewCar.setOnClickListener {
            findNavController().navigate(R.id.action_adminHomeFragment_to_adminCarListFragment)
        }
        binding.btnUserHistory.setOnClickListener {
            findNavController().navigate(R.id.action_adminHomeFragment_to_adminHistoryFragment)
        }

        binding.btnReport.setOnClickListener {
            findNavController().navigate(R.id.action_adminHomeFragment_to_adminReportsFragment)
        }
        binding.btnTechnicalSupport.setOnClickListener {
            findNavController().navigate(R.id.action_adminHomeFragment_to_adminSupportFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
