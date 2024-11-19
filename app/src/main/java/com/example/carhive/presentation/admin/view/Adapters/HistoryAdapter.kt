package com.example.carhive.presentation.admin.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.carhive.data.model.HistoryEntity
import com.example.carhive.data.model.UserEntity
import com.example.carhive.databinding.ItemHistoryBinding
import java.text.SimpleDateFormat
import java.util.*

class HistoryAdapter(
    private val historyList: List<HistoryEntity>,
    private val userMap: Map<String, UserEntity?>
) : RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    inner class HistoryViewHolder(private val binding: ItemHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(historyItem: HistoryEntity) {
            binding.historyDescription.text = historyItem.message ?: "Mensaje no disponible"
            binding.historyTimestamp.text = formatDate(historyItem.timestamp ?: 0L)
            binding.historyEventType.text = historyItem.eventType ?: "Tipo no especificado"

            val user = userMap[historyItem.userId]
            binding.historyUserName.text = "${user?.firstName ?: "Nombre desconocido"} ${user?.lastName ?: "Apellido desconocido"}"

            Glide.with(binding.root.context)
                .load(user?.imageUrl)
                .circleCrop()
                .into(binding.historyUserImage)
        }

        private fun formatDate(timestamp: Long): String {
            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            return sdf.format(Date(timestamp))
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val binding = ItemHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.bind(historyList[position])
    }

    override fun getItemCount(): Int = historyList.size
}
