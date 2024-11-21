package com.example.carhive.presentation.admin.view

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.carhive.R
import com.example.carhive.presentation.admin.viewModel.AdminUserBanViewModel
import com.example.carhive.data.model.UserEntity
import com.example.carhive.presentation.admin.view.Adapters.UserAdapterBan
import com.example.carhive.databinding.FragmentAdminBanUserListBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AdminUserBanFragment : Fragment() {

    private var _binding: FragmentAdminBanUserListBinding? = null
    private val binding get() = _binding!!

    private val banViewModel: AdminUserBanViewModel by viewModels()
    private lateinit var adapter: UserAdapterBan

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminBanUserListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = UserAdapterBan(listOf(),
            onBanClick = { user ->
                banViewModel.banUser(user.id, "${user.firstName} ${user.lastName}", user.email)
                sendEmail(user.email, "Account Banned", "Your account has been banned. Contact support for further details.")
                Toast.makeText(requireContext(), "User banned successfully", Toast.LENGTH_SHORT).show()
            },
            offBanClick = { user ->
                banViewModel.unbanUser(user.id, "${user.firstName} ${user.lastName}", user.email)
                sendEmail(user.email, "Account Unbanned", "Your account has been reinstated. You can now access the platform.")
                Toast.makeText(requireContext(), "User unbanned successfully", Toast.LENGTH_SHORT).show()
            },
            onDeleteClick = { user ->
                showDeleteConfirmationDialog(user)
            }
        )

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@AdminUserBanFragment.adapter
        }

        banViewModel.users.observe(viewLifecycleOwner) { userList ->
            adapter.updateData(userList)
        }

        binding.searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterUsers(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.bureturn.setOnClickListener {
            findNavController().navigate(R.id.action_adminUserBanFragment_to_adminHomeFragment)
        }
    }

    private fun showDeleteConfirmationDialog(user: UserEntity) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete user")
            .setMessage("Are you sure you want to delete ${user.firstName} ${user.lastName}? This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                banViewModel.deleteUser(user.id, "${user.firstName} ${user.lastName}", user.email)
                sendEmail(user.email, "Account Deleted", "Your account has been permanently deleted from our platform.")
                Toast.makeText(requireContext(), "User deleted successfully", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }


    private fun filterUsers(query: String) {
        val filteredList = banViewModel.users.value?.filter { user ->
            user.firstName.contains(query, ignoreCase = true) ||
                    user.lastName.contains(query, ignoreCase = true) ||
                    user.email.contains(query, ignoreCase = true)
        } ?: emptyList()
        adapter.updateData(filteredList)
    }
    private fun sendEmail(email: String, subject: String, message: String) {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, message)
        }
        try {
            startActivity(Intent.createChooser(intent, "Send Email"))
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error sending email", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
