package com.example.carhive.Presentation.admin.view.Adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.carhive.Data.model.UserEntity
import com.example.carhive.databinding.ItemUserBanBinding

class UserAdapterBan(
    private var userList: List<UserEntity>,
    private val onBanClick: (UserEntity) -> Unit,
    private val offBanClick: (UserEntity) -> Unit,
    private val onDeleteClick: (UserEntity) -> Unit
) : RecyclerView.Adapter<UserAdapterBan.UserBanViewHolder>() {

    class UserBanViewHolder(val binding: ItemUserBanBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserBanViewHolder {
        val binding = ItemUserBanBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserBanViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserBanViewHolder, position: Int) {
        val user = userList[position]

        // Cargar la imagen del usuario
        user.imageUrl?.let {
            Glide.with(holder.itemView.context) // Usar el contexto de la vista
                .load(it)
                .into(holder.binding.userImageView)
        }

        // Asignar el resto de la información
        holder.binding.firstNameText.text = user.firstName
        holder.binding.lastNameText.text = user.lastName
        holder.binding.emailText.text = user.email
        holder.binding.verifiedText.text = if (user.isverified) {
            "Verificado"
        } else {
            "No verificado"
        }
        holder.binding.idText.text = user.id
        holder.binding.rolText.text = when (user.role) {
            0 -> "ADMINISTRADOR"
            1 -> "VENDEDOR"
            else -> "USUARIO NORMAL"
        }

        // Botón de banear
        holder.binding.Userbaner.setOnClickListener {
            onBanClick(user)
        }

        // Botón de desbanear
        holder.binding.Userdesbaner.setOnClickListener {
            offBanClick(user)
        }

        // Botón de eliminar
        holder.binding.UserDeleted.setOnClickListener {
            onDeleteClick(user)
        }
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    // Actualizar la lista de usuarios
    fun updateData(newUsers: List<UserEntity>) {
        userList = newUsers
        notifyDataSetChanged()
    }
}
