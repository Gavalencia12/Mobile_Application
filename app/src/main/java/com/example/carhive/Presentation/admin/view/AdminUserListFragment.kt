package com.example.carhive.Presentation.admin.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.carhive.Presentation.admin.viewModel.AdminUserListViewModel
import com.example.carhive.databinding.FragmentAdminUserListBinding
import dagger.hilt.android.AndroidEntryPoint
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.carhive.R
import com.example.carhive.Data.model.UserEntity
import com.example.carhive.Presentation.admin.view.Adapters.UserAdapter
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

        // Inicializar el adaptador
        adapter = UserAdapter(listOf(),
            onVerifyClick = { user ->
                val dialog = UserDetailsDialogFragment(user)
                dialog.show(parentFragmentManager, "UserDetailsDialog")
            },
            onDeleteClick = { user -> // Lógica para eliminar
            }
        )

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@AdminUserListFragment.adapter
        }


        // Observar la lista de usuarios desde el ViewModel o directamente de Firebase
        viewModel.users.observe(viewLifecycleOwner) { userList ->
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
            Log.e("AdminUserListFragment", "Error al obtener usuarios", it)
        }

        // Navegación de regreso
        binding.bureturn.setOnClickListener {
            Log.d("AdminUserListFragment", "Navegando de regreso a AdminHomeFragment")
            findNavController().navigate(R.id.action_adminUserListFragment_to_adminHomeFragment)
        }
    }





    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
