package com.example.carhive.Presentation.admin.view

import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.carhive.Presentation.admin.viewModel.AdminUserBanViewModel
import com.example.carhive.Data.model.UserEntity
import com.example.carhive.Presentation.admin.view.Adapters.UserAdapterBan
import com.example.carhive.R
import com.example.carhive.databinding.FragmentAdminBanUserListBinding
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
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
                Toast.makeText(requireContext(), "User banned successfully ", Toast.LENGTH_SHORT).show()
                banViewModel.banUser(user.id)
            },
            offBanClick = { user ->
                Toast.makeText(requireContext(), "User unbanned successfully", Toast.LENGTH_SHORT).show()
                banViewModel.unbanUser(user.id)
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



        val database = FirebaseDatabase.getInstance().getReference("Users")
        database.get().addOnSuccessListener { dataSnapshot ->
            val userList = mutableListOf<UserEntity>()

            dataSnapshot.children.forEach { userSnapshot ->
                val user = userSnapshot.getValue(UserEntity::class.java)
                user?.let {
                    it.id = userSnapshot.key ?: ""
                    userList.add(it)
                }
            }

            adapter.updateData(userList)

            binding.searchInput.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    filterUsers(s.toString(), userList)
                }
                override fun afterTextChanged(s: Editable?) {}
            })

        }
        binding.bureturn.setOnClickListener {
            findNavController().navigate(R.id.action_adminUserBanFragment_to_adminHomeFragment)
        }
    }
    private fun showDeleteConfirmationDialog(user: UserEntity) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete user")
            .setMessage("Are you sure you want to delete the user ${user.firstName} ${user.lastName}? This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                deleteUserAndImage(user)
                Toast.makeText(requireContext(), "User Delete successfully", Toast.LENGTH_SHORT).show()

            }
            .setNegativeButton("Close", null)
            .show()
    }
    private fun deleteUserAndImage(user: UserEntity) {
        val database = FirebaseDatabase.getInstance().getReference("Users")
        val storage = FirebaseStorage.getInstance()

        val imageUrl = user.imageUrl

        if (!imageUrl.isNullOrEmpty()) {
            val imageRef = storage.getReferenceFromUrl(imageUrl)
            imageRef.delete()
                .addOnSuccessListener {
                    banViewModel.deleteUser(user.id)
                }

        } else {
            banViewModel.deleteUser(user.id)
        }
    }

    private fun filterUsers(query: String, userList: List<UserEntity>) {
        val filteredList = userList.filter { user ->
            user.firstName.contains(query, ignoreCase = true) ||
                    user.lastName.contains(query, ignoreCase = true) ||
                    user.email.contains(query, ignoreCase = true)
        }
        adapter.updateData(filteredList)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
