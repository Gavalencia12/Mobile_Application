package com.example.carhive.Presentation.user.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.carhive.Data.model.UserEntity
import com.example.carhive.MainActivity
import com.example.carhive.Presentation.user.viewModel.ProfileViewModel
import com.example.carhive.R
import com.example.carhive.databinding.FragmentUserProfileSellerBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProfileSellerFragment : Fragment() {
    private var _binding: FragmentUserProfileSellerBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProfileViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserProfileSellerBinding.inflate(inflater, container, false)
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
            // Navegar al userHomeFragment
            findNavController().navigate(R.id.action_userProfileFragment_to_userProfileFragment)

            // Actualizar la selección del BottomNavigationView
            (activity as MainActivity).bottomNavigationView.selectedItemId = R.id.profile // Cambia el ID al correspondiente
        }
        binding.btnSeller.setOnClickListener{
            findNavController().navigate(R.id.action_userProfileFragment_to_sellerHomeFragment)
        }
    }

    // Método para actualizar la UI con los datos del usuario
    private fun updateUserProfileUI(user: UserEntity) {

        if (user.isverified) {
            binding.tvVerified.text = "Estas Verificado" // Actualizar el nombre del usuario
            binding.ivIsVerified.visibility = View.VISIBLE
            binding.btnSeller.isEnabled = true

        } else {
            binding.tvVerified.text = "No Estas Verificado"
            binding.ivIsVerified.visibility = View.GONE
            binding.btnSeller.isEnabled = false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}