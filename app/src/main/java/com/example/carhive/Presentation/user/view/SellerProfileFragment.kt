package com.example.carhive.Presentation.seller.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import com.bumptech.glide.Glide
import com.example.carhive.Data.model.UserEntity
import com.example.carhive.MainActivity
import com.example.carhive.Presentation.user.adapter.ProfileOptionsAdapter
import com.example.carhive.Presentation.user.viewModel.ProfileViewModel
import com.example.carhive.R
import com.example.carhive.databinding.FragmentSellerProfileBinding
import com.example.carhive.databinding.FragmentUserProfileBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SellerProfileFragment : Fragment() {
    private var _binding: FragmentSellerProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProfileViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSellerProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Vincular el BottomNavigationView con el NavController
        val navController = findNavController()
        NavigationUI.setupWithNavController(binding.bottomNavigationSeller, navController)

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

//        binding.ibtnBack.setOnClickListener {
//            // Navegar al userHomeFragment
//            findNavController().navigate(R.id.action_userProfileFragment_to_userHomeFragment)
//            (activity as MainActivity).bottomNavigationViewSeller.selectedItemId = R.id.home
//        }

        // Configurar la lista de opciones del perfil
        val listView = binding.profileOptionsList
        val adapter = ProfileOptionsAdapter(requireContext(), viewModel.profileOptionsSeller, viewModel.profileIconsSeller)
        listView.adapter = adapter

        // Manejar los clics en las opciones del perfil
        listView.setOnItemClickListener { _, _, position, _ ->
            val option = viewModel.profileOptionsSeller[position]
            when (option) {
                "Log out" -> {
                    viewModel.logout()
                    findNavController().navigate(R.id.action_userProfileFragment_to_loginFragment)
                }
                "Terms & Conditions" -> {
                    findNavController().navigate(R.id.action_userProfileFragment_to_termsSellerFragment)
                }
                // Otros casos...
            }
        }
    }

    // Método para actualizar la UI con los datos del usuario
    private fun updateUserProfileUI(user: UserEntity) {
        binding.profileName.text = "${user.firstName} ${user.lastName}" // Actualizar el nombre del usuario
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
