package com.example.carhive.presentation.chat.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.carhive.R
import com.example.carhive.data.model.UserWithLastMessage

class SimpleUsersMessagesAdapter(
    private val items: MutableList<UserWithLastMessage>,
    private val onItemClicked: (UserWithLastMessage) -> Unit
) : RecyclerView.Adapter<SimpleUsersMessagesAdapter.MessageViewHolder>() {

    inner class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.userNameTextView)
        private val imageView: ImageView = itemView.findViewById(R.id.profileImageView)
        private val lastMessageTextView: TextView = itemView.findViewById(R.id.lastMessageTextView)

        fun bind(item: UserWithLastMessage) {
            nameTextView.text = item.user.firstName
            lastMessageTextView.text = item.lastMessage
            Glide.with(itemView.context)
                .load(item.user.imageUrl)
                .placeholder(R.drawable.ic_person)
                .error(R.drawable.ic_error)
                .into(imageView)

            itemView.setOnClickListener { onItemClicked(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_interested_user, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun updateData(newItems: List<UserWithLastMessage>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }
}
