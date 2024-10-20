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

        database = FirebaseDatabase.getInstance().getReference("Users")

        // Mostrar los datos del usuario
        binding.firstNameText.text = user.firstName
        binding.lastNameText.text = user.lastName
        binding.emailText.text = user.email
        binding.phoneNumberText.text = user.phoneNumber
        binding.voterIDText.text = user.voterID
        binding.curpText.text = user.curp
        binding.verifiedText.text = if (user.isverified) {
            "Verified"
        } else {
            "Not Verified"
        }


        user.imageUrl?.let {
            Glide.with(this).load(it).into(binding.userImageView)
        }

        binding.closeButton.setOnClickListener {
            dismiss()
        }

        binding.userverified.setOnClickListener {
            verifyUser()
        }
        binding.Desactivate.setOnClickListener {
            desactivateUser()
        }
    }

    private fun verifyUser() {
        user.isverified = true
        user.verificationTimestamp = System.currentTimeMillis().toString()


        val userRef = database.child(user.id)

        userRef.child("isverified").setValue(true).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                userRef.child("verificationTimestamp").setValue(user.verificationTimestamp)
                Toast.makeText(requireContext(), "User successfully verified", Toast.LENGTH_SHORT).show()
                dismiss()
            } else {
                Toast.makeText(requireContext(), "Error verifying the user", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun desactivateUser() {
        user.isverified = false
        user.verificationTimestamp = System.currentTimeMillis().toString()


        val userRef = database.child(user.id)

        userRef.child("isverified").setValue(false).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                userRef.child("verificationTimestamp").setValue(user.verificationTimestamp)
                Toast.makeText(requireContext(), "User successfully deactivated", Toast.LENGTH_SHORT).show()
                dismiss()
            } else {
                Toast.makeText(requireContext(), "Error deactivating the user", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
