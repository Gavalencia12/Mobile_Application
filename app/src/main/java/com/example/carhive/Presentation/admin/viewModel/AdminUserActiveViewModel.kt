package com.example.carhive.Presentation.admin.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.*
import com.example.carhive.Data.model.UsuariosActivos


class AdminUserActiveViewModel : ViewModel() {

    private val database: DatabaseReference = FirebaseDatabase.getInstance().getReference("usuariosActivos")

    private val _usuariosActivos = MutableLiveData<List<UsuariosActivos>>()
    val usuariosActivos: LiveData<List<UsuariosActivos>> get() = _usuariosActivos

    init {
        escucharUsuariosActivos()
    }

    private fun escucharUsuariosActivos() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val listaUsuarios = mutableListOf<UsuariosActivos>()
                for (data in snapshot.children) {
                    val usuarioActivo = data.getValue(UsuariosActivos::class.java)
                    usuarioActivo?.let { listaUsuarios.add(it) }
                }
                _usuariosActivos.value = listaUsuarios
            }

            override fun onCancelled(error: DatabaseError) {
                // Manejo de errores
            }
        })
    }
}
