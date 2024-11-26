package com.example.carhive.presentation.user.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.carhive.data.model.UserEntity
import com.example.carhive.presentation.user.adapter.ProfileOptionsAdapter
import com.example.carhive.presentation.user.viewModel.ProfileViewModel
import com.example.carhive.R
import com.example.carhive.databinding.FragmentUserProfileBinding
import com.example.carhive.presentation.user.items.UpdateDataDialogFragment
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProfileFragment : Fragment() {
    private var _binding: FragmentUserProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProfileViewModel by viewModels()

    val buyerId = FirebaseAuth.getInstance().currentUser?.uid

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Escuchar el resultado del diálogo
        parentFragmentManager.setFragmentResultListener("update_request_key", this) { _, _ ->
            // Recargar los datos del usuario después de actualizar
            viewModel.fetchUserData()
        }

        // Observar los datos del usuario
        viewModel.userData.observe(viewLifecycleOwner, Observer { result ->
            result.onSuccess { userList ->
                if (userList.isNotEmpty()) {
                    val user = userList[0] // Asumiendo que solo hay un usuario
                    updateUserProfileUI(user) // Método para actualizar la UI con los datos del usuario
                }
            }.onFailure {
                // Muestra un error si algo falla
                Toast.makeText(context, "Error al obtener los datos del usuario", Toast.LENGTH_SHORT).show()
            }
        })

        // Iniciar la obtención de los datos del usuario
        viewModel.fetchUserData()

        binding.ibtnBack.setOnClickListener {
            findNavController().navigate(R.id.action_userProfileFragment_to_userHomeFragment)
        }
        binding.updateData.setOnClickListener {
            val dialog = UpdateDataDialogFragment()
            dialog.show(parentFragmentManager, "UpdateDataDialog")
        }


        // Configurar la lista de opciones del perfil
        val listView = binding.profileOptionsList
        val adapter = ProfileOptionsAdapter(requireContext(), viewModel.profileOptions, viewModel.profileIcons)
        listView.adapter = adapter

        // Manejar los clics en las opciones del perfil
        listView.setOnItemClickListener { _, _, position, _ ->
            val option = viewModel.profileOptions[position]
            when (option) {
                "Log out" -> {
                    viewModel.logout()
                    findNavController().navigate(R.id.action_userProfileFragment_to_loginFragment)
                }
                "Do you want to become a seller?" -> {
                    findNavController().navigate(R.id.action_userProfileFragment_to_profileSellerFragment)
                }
                "Terms & Conditions" -> {
                    findNavController().navigate(R.id.action_userProfileFragment_to_termsFragment)
                }
                "Privacy policy" -> {
                    findNavController().navigate(R.id.action_userProfileFragment_to_privacyPolicyFragment)
                }
                "Technical Support" -> {
                    val bundle = Bundle().apply {
                        putString("carId", "buyer")
                        putString("ownerId", "TechnicalSupport")
                        putString("buyerId", buyerId)
                    }
                    findNavController().navigate(R.id.action_userProfileFragment_to_UserReportMessagesFragment, bundle)
                }
                "Personal Data" -> {
                    findNavController().navigate(R.id.action_userProfileFragment_to_PersonalDataFragment)
                }
                "Comment History" -> {
                    findNavController().navigate(R.id.action_userProfileFragment_to_CommentHistoryFragment)
                }
                "Recommendations" -> { // Manejar la opción "Recommendations"
                val dialog = RecommendationsDialogFragmentUser()
                dialog.show(parentFragmentManager, "RecommendationsDialog")
            }
                // Otros casos...
            }
        }
    }

    // Método para actualizar la UI con los datos del usuario
    private fun updateUserProfileUI(user: UserEntity) {
        binding.profileName.text = "${ user.firstName } ${user.lastName}" // Actualizar el nombre del usuario
        if (user.isverified) {
            binding.ivIsVerified.visibility = View.VISIBLE
        } else {
            binding.ivIsVerified.visibility = View.GONE
        }
        Glide.with(this).load(user.imageUrl).placeholder(R.drawable.ic_profile).error(R.drawable.ic_profile).circleCrop().into(binding.profileImage)

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
