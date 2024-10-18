package com.example.carhive.Presentation.admin.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

        // Inicializar el adaptador
        adapter = UserAdapterBan(listOf(),
            onBanClick = { user ->
                banViewModel.banUser(user.id)
            },
            offBanClick = { user ->
                banViewModel.unbanUser(user.id)
            },
            onDeleteClick = { user ->
                banViewModel.deleteUser(user.id)
            }
        )

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@AdminUserBanFragment.adapter
        }
        //Observar la lista de usuarios desde el ViewModel o directamente de Firebase
        banViewModel.users.observe(viewLifecycleOwner) { userList ->
            adapter.updateData(userList)
        }

        // Lógica para obtener usuarios con ID desde Firebase
        val database = FirebaseDatabase.getInstance().getReference("Users")
        database.get().addOnSuccessListener { dataSnapshot ->
            val userList = mutableListOf<UserEntity>()

            dataSnapshot.children.forEach { userSnapshot ->
                val user = userSnapshot.getValue(UserEntity::class.java)
                user?.let {
                    // Asignar el ID del nodo al objeto UserEntity
                    it.id = userSnapshot.key ?: ""
                    userList.add(it)
                }
            }

            // Actualizar la lista en el adaptador
            adapter.updateData(userList)

        }.addOnFailureListener {
            // Manejar el error en caso de que falle la recuperación de datos
            Log.e("AdminUserBanFragment", "Error al obtener usuarios", it)
        }
        binding.bureturn.setOnClickListener {
            Log.d("AdminUserBanFragment", "Navegando de regreso a AdminHomeFragment")
            findNavController().navigate(R.id.action_adminUserBanFragment_to_adminHomeFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
