package com.example.carhive.presentation.admin.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.carhive.data.model.UserEntity
import com.google.firebase.database.*
import java.time.Instant
import java.time.ZoneId

class AdminUserActiveViewModel : ViewModel() {
    private val database: DatabaseReference = FirebaseDatabase.getInstance().getReference("Users")

    private val _usuarios = MutableLiveData<List<UserEntity>>()
    val usuarios: LiveData<List<UserEntity>> get() = _usuarios

    init {
        escucharUsuarios()
    }

    private fun escucharUsuarios() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val listaUsuarios = mutableListOf<UserEntity>()
                for (data in snapshot.children) {
                    val usuario = data.getValue(UserEntity::class.java)
                    usuario?.let { listaUsuarios.add(it) }
                }
                _usuarios.value = listaUsuarios // Actualizamos el LiveData
            }

            override fun onCancelled(error: DatabaseError) {
                // Manejo de errores
            }
        })
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun obtenerConteoUsuariosPorDia(): Pair<List<Int>, List<Int>> {
        val verificadosPorDia = IntArray(7) { 0 } // Un array para los 7 días de la semana
        val desverificadosPorDia = IntArray(7) { 0 }

        _usuarios.value?.forEach { usuario ->
            val diaDeLaSemana = Instant.ofEpochMilli(usuario.verificationTimestamp!!.toLong()).atZone(ZoneId.systemDefault()).dayOfWeek.value - 1 // De 0 a 6
            if (usuario.isverified) {
                verificadosPorDia[diaDeLaSemana]++
            } else {
                desverificadosPorDia[diaDeLaSemana]++
            }
        }

        return Pair(verificadosPorDia.toList(), desverificadosPorDia.toList())
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun obtenerConteoUsuariosPorMes(): Pair<List<Int>, List<Int>> {
        val verificadosPorMes = IntArray(12) { 0 } // Un array para los 12 meses
        val desverificadosPorMes = IntArray(12) { 0 }

        _usuarios.value?.forEach { usuario ->
            val mesDelAño = Instant.ofEpochMilli(usuario.verificationTimestamp!!.toLong()).atZone(ZoneId.systemDefault()).monthValue - 1 // De 0 a 11
            if (usuario.isverified) {
                verificadosPorMes[mesDelAño]++
            } else {
                desverificadosPorMes[mesDelAño]++
            }
        }

        return Pair(verificadosPorMes.toList(), desverificadosPorMes.toList())
    }
}