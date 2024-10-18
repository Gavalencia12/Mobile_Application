package com.example.carhive.Presentation.admin.view

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.example.carhive.Data.model.UserEntity
import com.example.carhive.databinding.FragmentUserDetailsDialogBinding
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase


class UserDetailsDialogFragment(private val user: UserEntity) : DialogFragment() {

    private var _binding: FragmentUserDetailsDialogBinding? = null
    private val binding get() = _binding!!

    // Referencia a la base de datos
    private lateinit var database: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserDetailsDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicializa la referencia a la base de datos de Firebase
        database = FirebaseDatabase.getInstance().getReference("Users")

        // Mostrar los datos del usuario
        binding.firstNameText.text = user.firstName
        binding.lastNameText.text = user.lastName
        binding.emailText.text = user.email
        binding.phoneNumberText.text = user.phoneNumber
        binding.voterIDText.text = user.voterID
        binding.curpText.text = user.curp
        // Verificación del estado de usuario
        binding.verifiedText.text = if (user.isverified) {
            "Verificado"
        } else {
            "No verificado"
        }


        // Cargar la imagen del usuario
        user.imageUrl?.let {
            Glide.with(this).load(it).into(binding.userImageView)
        }

        // Botón para cerrar el modal
        binding.closeButton.setOnClickListener {
            dismiss()
        }

        // Botón para verificar al usuario
        binding.userverified.setOnClickListener {
            verifyUser()
        }
        // Botón para verificar al usuario
        binding.Desactivate.setOnClickListener {
            desactivateUser()
        }
    }

    private fun verifyUser() {
        // Cambiar el estado de verificación del usuario
        user.isverified = true


        // Referencia al usuario en Firebase utilizando el ID del usuario
        val userRef = database.child(user.id)

        // Actualizar solo el campo isverified
        userRef.child("isverified").setValue(true).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(requireContext(), "Usuario verificado exitosamente.", Toast.LENGTH_SHORT).show()
                dismiss()  // Cierra el diálogo después de la verificación
            } else {
                Toast.makeText(requireContext(), "Error al verificar al usuario.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun desactivateUser() {
        // Cambiar el estado de verificación del usuario
        user.isverified = false


        // Referencia al usuario en Firebase utilizando el ID del usuario
        val userRef = database.child(user.id)

        // Actualizar solo el campo isverified
        userRef.child("isverified").setValue(false).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(requireContext(), "Usuario desactivado exitosamente.", Toast.LENGTH_SHORT).show()
                dismiss()  // Cierra el diálogo después de la verificación
            } else {
                Toast.makeText(requireContext(), "Error al desactivar al usuario.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
