package com.example.carhive.presentation.admin.view

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.carhive.presentation.admin.viewModel.AdminUserListViewModel
import com.example.carhive.databinding.FragmentAdminUserListBinding
import dagger.hilt.android.AndroidEntryPoint
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.carhive.R
import com.example.carhive.data.model.UserEntity
import com.example.carhive.presentation.admin.view.Adapters.UserAdapter
import com.google.firebase.database.FirebaseDatabase


@AndroidEntryPoint
class AdminUserListFragment : Fragment() {

    private var _binding: FragmentAdminUserListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AdminUserListViewModel by viewModels()

    private lateinit var adapter: UserAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminUserListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = UserAdapter(listOf(),
            onVerifyClick = { user ->
                val dialog = UserDetailsDialogFragment(user){
                    viewModel.getUsers()
                }
                dialog.show(parentFragmentManager, "UserDetailsDialog")
            },
            onDeleteClick = { user ->
            }
        )

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@AdminUserListFragment.adapter
        }


        viewModel.users.observe(viewLifecycleOwner) { userList ->
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
            findNavController().navigate(R.id.action_adminUserListFragment_to_adminHomeFragment)
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
