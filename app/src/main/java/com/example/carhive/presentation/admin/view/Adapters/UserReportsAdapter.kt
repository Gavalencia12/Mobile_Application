package com.example.carhive.presentation.admin.view.Adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.carhive.R
import com.example.carhive.data.model.UserEntity
import com.example.carhive.data.model.UserReport
import com.example.carhive.databinding.ItemUserReportBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class UserReportsAdapter(
    private val userMap: Map<String, UserEntity?>,
    private val onItemClick: (UserReport) -> Unit
) : RecyclerView.Adapter<UserReportsAdapter.ViewHolder>() {

    private val reports = mutableListOf<UserReport>()

    fun submitList(newReports: List<UserReport>) {
        reports.clear()
        reports.addAll(newReports)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemUserReportBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val report = reports[position]
        holder.bind(report)
    }

    override fun getItemCount(): Int = reports.size

    inner class ViewHolder(private val binding: ItemUserReportBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(report: UserReport) {
            val reporter = userMap[report.userId]
            val reported = userMap[report.reportedUserId]

            // Usuario que reportó
            binding.userName.text = reporter?.firstName ?: "Unknown Reporter"
            Glide.with(binding.userImage.context)
                .load(reporter?.imageUrl ?: R.drawable.ic_image)
                .circleCrop()
                .into(binding.userImage)

            // Usuario reportado
            binding.reportedUserId.text = "user reported: ${reported?.firstName ?: " Admin "}"

            // Detalles del reporte
            binding.reportDescription.text = report.comment
            binding.reportTimestamp.text = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                .format(Date(report.timestamp))

            // Acción al hacer clic
            binding.root.setOnClickListener { onItemClick(report) }
        }
    }
}
