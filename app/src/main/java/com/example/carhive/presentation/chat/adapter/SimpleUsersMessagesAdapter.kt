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
        private val fileIconImageView: ImageView = itemView.findViewById(R.id.fileIconImageView)
        private val unreadCountTextView: TextView = itemView.findViewById(R.id.unreadCountTextView)

        /**
         * Binds a UserWithLastMessage item to the ViewHolder.
         * Loads the user's profile image, displays their name and last message, and sets an item click listener.
         *
         * @param item The UserWithLastMessage object to bind.
         */
        fun bind(item: UserWithLastMessage) {
            nameTextView.text = item.user.firstName
            setFileIconAndMessage(item.fileType, item.isFile, item.lastMessage, item.fileName)
            Glide.with(itemView.context)
                .load(item.user.imageUrl)
                .placeholder(R.drawable.ic_person)
                .error(R.drawable.ic_error)
                .into(imageView)
            if (item.unreadCount > 0) {
                unreadCountTextView.visibility = View.VISIBLE
                unreadCountTextView.text = item.unreadCount.toString()
            } else {
                unreadCountTextView.visibility = View.GONE
            }

            itemView.setOnClickListener { onItemClicked(item) }
        }
        private fun setFileIconAndMessage(fileType: String?, isFile: Boolean, messageText: String, fileName: String?) {
            if (isFile) {
                fileIconImageView.visibility = View.VISIBLE
                lastMessageTextView.text = fileName ?: messageText
                when {
                    fileType?.contains("application") == true -> {
                        fileIconImageView.setImageResource(R.drawable.ic_file)
                    }
                    fileType?.contains("image") == true -> {
                        fileIconImageView.setImageResource(R.drawable.ic_image)
                        lastMessageTextView.text = "Image"
                    }
                    fileType?.contains("video") == true -> {
                        fileIconImageView.setImageResource(R.drawable.ic_videos)
                        lastMessageTextView.text = "Video"
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
