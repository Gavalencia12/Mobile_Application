package com.example.carhive.Presentation.admin.view.Adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.carhive.Data.model.UserEntity
import com.example.carhive.databinding.ItemUserBinding

class UserAdapter(private var userList: List<UserEntity>, private val onVerifyClick: (UserEntity) -> Unit,
                  private val onDeleteClick: (UserEntity) -> Unit) :
    RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    class UserViewHolder(val binding: ItemUserBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = ItemUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = userList[position]
        // Cargar la imagen del usuario
        user.imageUrl?.let {
            Glide.with(holder.itemView.context) // Usar el contexto de la vista
                .load(it)
                .into(holder.binding.userImageView)
        }
        holder.binding.firstNameText.text = user.firstName
        holder.binding.lastNameText.text = user.lastName
        holder.binding.emailText.text = user.email
        holder.binding.verifiedText.text = if (user.isverified) {
            "Verificado"
        } else {
            "No verificado"
        }
        holder.binding.idText.text = user.id
        holder.binding.rolText.text = if (user.role == 0) {
            "ADMINISTRADOR"
        } else if (user.role == 1){
            "VENDEDOR"
        } else{
            "USUARIO NORMAL"
        }

        // Botón de verificación
        holder.binding.verification.setOnClickListener {
            onVerifyClick(user)
        }

    }

    override fun getItemCount(): Int {
        return userList.size
    }

    fun updateData(newUsers: List<UserEntity>) {
        userList = newUsers
        notifyDataSetChanged()
    }
}

