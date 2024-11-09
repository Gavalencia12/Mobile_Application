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

/**
 * RecyclerView adapter for displaying a list of users with their last message.
 * This adapter handles user interactions and displays the user's name, profile image, and last message.
 *
 * @param items List of UserWithLastMessage objects to be displayed.
 * @param onItemClicked Lambda function to handle item click events.
 */
class SimpleUsersMessagesAdapter(
    private val items: MutableList<UserWithLastMessage>,
    private val onItemClicked: (UserWithLastMessage) -> Unit
) : RecyclerView.Adapter<SimpleUsersMessagesAdapter.MessageViewHolder>() {

    /**
     * ViewHolder class for displaying individual user items in the RecyclerView.
     */
    inner class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.userNameTextView)
        private val imageView: ImageView = itemView.findViewById(R.id.profileImageView)
        private val lastMessageTextView: TextView = itemView.findViewById(R.id.lastMessageTextView)

        /**
         * Binds a UserWithLastMessage item to the ViewHolder.
         * Loads the user's profile image, displays their name and last message, and sets an item click listener.
         *
         * @param item The UserWithLastMessage object to bind.
         */
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

    /**
     * Creates a new ViewHolder when there are no existing ViewHolders available to reuse.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_interested_user, parent, false)
        return MessageViewHolder(view)
    }

    /**
     * Binds data to the ViewHolder at the specified position in the RecyclerView.
     */
    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        holder.bind(items[position])
    }

    /**
     * Returns the total number of items to be displayed in the RecyclerView.
     */
    override fun getItemCount(): Int = items.size

    /**
     * Updates the list of items and notifies the adapter to refresh the view.
     *
     * @param newItems The new list of UserWithLastMessage objects to display.
     */
    fun updateData(newItems: List<UserWithLastMessage>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }
}
