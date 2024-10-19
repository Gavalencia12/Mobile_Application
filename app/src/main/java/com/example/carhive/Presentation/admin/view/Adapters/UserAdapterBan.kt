package com.example.carhive.Presentation.admin.view.Adapters

import android.view.LayoutInflater
import android.view.View
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

    inner class UserBanViewHolder(val binding: ItemUserBanBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(user: UserEntity) {
            // Cargar la imagen del usuario
            user.imageUrl?.let {
                Glide.with(binding.root.context)
                    .load(it)
                    .into(binding.userImageView)
            }

            // Asignar la información básica
            binding.firstNameText.text = user.firstName
            binding.lastNameText.text = user.lastName
            binding.idText.text = user.id

            // Asignar la información expandida
            binding.emailText.text = user.email
            binding.verifiedText.text = if (user.isverified) {
                "Verificado"
            } else {
                "No verificado"
            }
            binding.rolText.text = when (user.role) {
                0 -> "ADMINISTRADOR"
                1 -> "VENDEDOR"
                else -> "USUARIO NORMAL"
            }

            // Configurar la visibilidad inicial del contenedor expandido
            binding.expandedInfoContainer.visibility = if (user.isExpanded) {
                View.VISIBLE
            } else {
                View.GONE
            }

            // Manejar el clic para expandir y contraer
            binding.basicInfoContainer.setOnClickListener {
                user.isExpanded = !user.isExpanded
                notifyItemChanged(adapterPosition)
            }

            // Configurar los botones
            binding.Userbaner.setOnClickListener {
                onBanClick(user)
            }

            binding.Userdesbaner.setOnClickListener {
                offBanClick(user)
            }

            binding.UserDeleted.setOnClickListener {
                onDeleteClick(user)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserBanViewHolder {
        val binding = ItemUserBanBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserBanViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserBanViewHolder, position: Int) {
        val user = userList[position]
        holder.bind(user)
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
