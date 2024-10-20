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
import com.example.carhive.Presentation.user.items.TermsAndConditionsDialogSeller
import com.example.carhive.Presentation.user.viewModel.ProfileSellerViewModel
import com.example.carhive.R
import com.example.carhive.databinding.FragmentUserProfileSellerBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProfileSellerFragment : Fragment() {
    private var _binding: FragmentUserProfileSellerBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProfileSellerViewModel by viewModels()

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

        // Listener para el botón de "Atrás"
        binding.ibtnBack.setOnClickListener {
            // Navegar al userHomeFragment
            findNavController().navigate(R.id.action_userProfileFragment_to_userProfileFragment)

            // Actualizar la selección del BottomNavigationView
//            (activity as MainActivity).bottomNavigationView.selectedItemId = R.id.profile // Cambia el ID al correspondiente
        }

        // Listener para el CheckBox
        binding.cbTerms.setOnCheckedChangeListener { _, isChecked ->
            toggleSellerButton() // Llamar para verificar si el botón debe estar habilitado
        }

        // Listener para el botón de "Convertir a Vendedor"
        binding.btnSeller.setOnClickListener {
            viewModel.saveRolSeller()
            viewModel.saveTermsSeller()
            findNavController().navigate(R.id.action_userProfileFragment_to_sellerHomeFragment)
        }

        // Mostrar el modal de términos y condiciones cuando se hace clic en el TextView
        binding.termsTextView.setOnClickListener {
            val termsDialog = TermsAndConditionsDialogSeller()
            termsDialog.show(parentFragmentManager, "TermsAndConditionsDialogSeller")
        }
    }

    // Método para actualizar la UI con los datos del usuario
    private fun updateUserProfileUI(user: UserEntity) {
        // Actualizar los datos de verificación
        if (user.isverified) {
            binding.tvVerified.text = "Estás Verificado"
            binding.ivIsVerified.visibility = View.VISIBLE
        } else {
            binding.tvVerified.text = "No estás Verificado"
            binding.ivIsVerified.visibility = View.GONE
        }

        // Llamar para verificar si el botón debe estar habilitado
        toggleSellerButton(user.isverified)
    }

    // Método para habilitar o deshabilitar el botón btnSeller
    private fun toggleSellerButton(isVerified: Boolean? = null) {
        val verified = isVerified ?: viewModel.userData.value?.getOrNull()?.firstOrNull()?.isverified == true
        binding.btnSeller.isEnabled = verified && binding.cbTerms.isChecked
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
