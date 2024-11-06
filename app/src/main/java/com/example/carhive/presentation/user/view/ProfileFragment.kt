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
import com.bumptech.glide.Glide // Asegúrate de tener Glide para cargar imágenes
import com.example.carhive.data.model.UserEntity
import com.example.carhive.MainActivity
import com.example.carhive.presentation.user.adapter.ProfileOptionsAdapter
import com.example.carhive.presentation.user.viewModel.ProfileViewModel
import com.example.carhive.R
import com.example.carhive.databinding.FragmentUserProfileBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProfileFragment : Fragment() {
    private var _binding: FragmentUserProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProfileViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
        Glide.with(this).load(user.imageUrl).circleCrop().into(binding.profileImage)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
