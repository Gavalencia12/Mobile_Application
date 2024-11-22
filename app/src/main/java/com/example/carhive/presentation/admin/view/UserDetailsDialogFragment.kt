package com.example.carhive.presentation.admin.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.carhive.data.datasource.remote.NotificationsRepositoryImpl
import com.example.carhive.data.model.HistoryEntity
import com.example.carhive.data.model.UserEntity
import com.example.carhive.databinding.FragmentUserDetailsDialogBinding
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.launch

class UserDetailsDialogFragment(private val user: UserEntity,private val onUserUpdated: () -> Unit ) : DialogFragment() {

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

        user.imageUrl2?.let {
            Glide.with(this).load(it).into(binding.userImageView)
        }

        binding.closeButton.setOnClickListener {
            dismiss()
        }

        binding.userverified.setOnClickListener {
            verifyUser()
        }
        binding.Desactivate.setOnClickListener {
            deactivateUser()
        }
    }

    private fun verifyUser() {
        user.isverified = true
        user.verificationTimestamp = System.currentTimeMillis().toString()

        val userRef = database.child(user.id)

        userRef.child("isverified").setValue(true).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                userRef.child("verificationTimestamp").setValue(user.verificationTimestamp)
                addHistoryEntry(
                    eventType = "Verification",
                    message = "User ${user.firstName} was verified"
                )
                sendNotification(true)
                Toast.makeText(requireContext(), "User successfully verified", Toast.LENGTH_SHORT).show()
                onUserUpdated()
                dismiss()
            } else {
                Toast.makeText(requireContext(), "Error verifying the user", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun deactivateUser() {
        user.isverified = false
        user.verificationTimestamp = System.currentTimeMillis().toString()

        val userRef = database.child(user.id)

        userRef.child("isverified").setValue(false).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                userRef.child("verificationTimestamp").setValue(user.verificationTimestamp)
                addHistoryEntry(
                    eventType = "Deactivation",
                    message = "User ${user.firstName} was deactivated"
                )
                sendNotification(false)
                Toast.makeText(requireContext(), "User successfully deactivated", Toast.LENGTH_SHORT).show()
                onUserUpdated()
                dismiss()
            } else {
                Toast.makeText(requireContext(), "Error deactivating the user", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun sendNotification(isVerified: Boolean) {
        val fullName = "${user.firstName} ${user.lastName}"
        viewLifecycleOwner.lifecycleScope.launch {
            val repository = NotificationsRepositoryImpl(requireContext(), FirebaseDatabase.getInstance())
            repository.listenForUserVerification(user.id, isVerified, fullName)
        }
    }

    private fun addHistoryEntry(eventType: String, message: String) {
        val historyEntry = HistoryEntity(
            userId = user.id,
            timestamp = System.currentTimeMillis(),
            eventType = eventType,
            message = message
        )

        val historyRef = FirebaseDatabase.getInstance().getReference("History/userHistory")
        historyRef.push().setValue(historyEntry).addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Toast.makeText(requireContext(), "Failed to log history entry", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
