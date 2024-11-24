package com.example.carhive.presentation.user.view


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.carhive.databinding.FragmentAdditionalDataBinding
import com.example.carhive.presentation.user.viewModel.AdditionalDataViewModel
import com.bumptech.glide.Glide
import com.example.carhive.R
import com.example.carhive.presentation.user.items.UpdateDataDialogFragment

class AdditionalDataFragment : Fragment() {

    private lateinit var binding: FragmentAdditionalDataBinding
    private val viewModel: AdditionalDataViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentAdditionalDataBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Observar los datos combinados
        viewModel.combinedData.observe(viewLifecycleOwner) { combinedData ->
            combinedData?.let { (additionalData, user) ->
                // Mostrar datos de AdditionalDataEntity
                binding.textAddress.text = additionalData.address
                binding.textInterior.text = additionalData.interiorNumber
                binding.textOuter.text = additionalData.outerNumber
                binding.textSex.text = additionalData.sex
                binding.textDescription.text = additionalData.description

                // Mostrar datos de UserEntity
                binding.profileName.text = "${user.firstName} ${user.lastName}"
                binding.textEmail.text = user.email
                binding.textPhoneNumber.text = user.phoneNumber
                Glide.with(this)
                    .load(user.imageUrl)
                    .circleCrop()
                    .into(binding.profileImage)
            }
        }

        viewModel.loadUserData()

        binding.ibtnBack.setOnClickListener {
            viewModel.combinedData.observe(viewLifecycleOwner) { combinedData ->
                combinedData?.let { (_, user) ->
                    when (user.role) {
                        1 -> findNavController().navigate(R.id.action_PersonalDataFragment_to_sellerProfileFragment)
                        2 -> findNavController().navigate(R.id.action_PersonalDataFragment_to_userProfileFragment)
                    }
                }
            }
        }


        binding.updateData.setOnClickListener {
            val dialog = UpdateDataDialogFragment()
            dialog.show(parentFragmentManager, "UpdateDataDialog")
        }

    }
}