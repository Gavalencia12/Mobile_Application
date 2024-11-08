package com.example.carhive.presentation.chat.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.carhive.R
import com.example.carhive.data.model.UserWithLastMessage
import com.example.carhive.data.model.CarWithLastMessage

class UsersMessagesAdapter(
    private var recentItems: MutableList<Any>,
    private var otherItems: MutableList<Any>,
    private val onItemClicked: (Any) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_RECENT_HEADER = 0
        private const val VIEW_TYPE_OTHER_HEADER = 1
        private const val VIEW_TYPE_ITEM = 2
    }

    inner class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.userNameTextView)
        private val imageView: ImageView = itemView.findViewById(R.id.profileImageView)
        private val lastMessageTextView: TextView = itemView.findViewById(R.id.lastMessageTextView)
        private val fileIconImageView: ImageView = itemView.findViewById(R.id.fileIconImageView)

        fun bind(item: Any) {
            when (item) {
                is UserWithLastMessage -> {
                    nameTextView.text = item.user.firstName
                    setFileIconAndMessage(item.fileType, item.isFile, item.lastMessage, item.fileName)
                    Glide.with(itemView.context)
                        .load(item.user.imageUrl)
                        .placeholder(R.drawable.ic_person)
                        .error(R.drawable.ic_error)
                        .into(imageView)
                }
                is CarWithLastMessage -> {
                    nameTextView.text = item.car.modelo
                    setFileIconAndMessage(item.fileType, item.isFile, item.lastMessage, item.fileName)
                    val imageUrl = item.car.imageUrls?.firstOrNull()
                    Glide.with(itemView.context)
                        .load(imageUrl)
                        .placeholder(R.drawable.ic_img)
                        .error(R.drawable.ic_error)
                        .into(imageView)
                }
            }
            itemView.setOnClickListener { onItemClicked(item) }
        }

        private fun setFileIconAndMessage(fileType: String?, isFile: Boolean, messageText: String, fileName: String?) {
            if (isFile) {
                fileIconImageView.visibility = View.VISIBLE
                lastMessageTextView.text = fileName ?: messageText
                when {
                    fileType?.contains("application") == true -> {
                        fileIconImageView.setImageResource(R.drawable.ic_video)
                    }
                    fileType?.contains("image") == true -> {
                        fileIconImageView.setImageResource(R.drawable.ic_img)
                    }
                    fileType?.contains("video") == true -> {
                        fileIconImageView.setImageResource(R.drawable.ic_video)
                    }
                    else -> {
                        fileIconImageView.visibility = View.GONE
                        lastMessageTextView.text = messageText
                    }
                }
            } else {
                fileIconImageView.visibility = View.GONE
                lastMessageTextView.text = messageText
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (position) {
            0 -> VIEW_TYPE_RECENT_HEADER
            recentItems.size + 1 -> VIEW_TYPE_OTHER_HEADER
            else -> VIEW_TYPE_ITEM
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_RECENT_HEADER, VIEW_TYPE_OTHER_HEADER -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_section_header, parent, false)
                SectionHeaderViewHolder(view)
            }
            else -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_interested_user, parent, false)
                MessageViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is SectionHeaderViewHolder -> holder.bind(if (position == 0) "Recent Chats" else "Other Chats")
            is MessageViewHolder -> {
                val item = if (position <= recentItems.size) {
                    recentItems[position - 1]
                } else {
                    otherItems[position - recentItems.size - 2]
                }
                holder.bind(item)
            }
        }
    }

    override fun getItemCount(): Int = recentItems.size + otherItems.size + 2

    fun updateData(newRecentItems: List<Any>, newOtherItems: List<Any>) {
        Log.i("Adapter", "Updating Adapter - Recent Items: ${newRecentItems.size}, Other Items: ${newOtherItems.size}")
        recentItems.clear()
        recentItems.addAll(newRecentItems)
        otherItems.clear()
        otherItems.addAll(newOtherItems)
        notifyDataSetChanged()
    }

    inner class SectionHeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val headerTextView: TextView = itemView.findViewById(R.id.sectionHeaderTextView)
        fun bind(headerText: String) {
            headerTextView.text = headerText
        }
    }
}
