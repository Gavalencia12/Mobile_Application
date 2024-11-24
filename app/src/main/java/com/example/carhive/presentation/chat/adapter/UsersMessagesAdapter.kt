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

/**
 * RecyclerView adapter for displaying recent and other user/car messages.
 * Each section can display a header and a list of either users or cars with their last message.
 *
 * @param recentItems List of recent UserWithLastMessage or CarWithLastMessage objects.
 * @param otherItems List of other UserWithLastMessage or CarWithLastMessage objects.
 * @param onItemClicked Lambda function to handle item click events.
 */
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

    /**
     * ViewHolder for displaying individual user or car items.
     */
    inner class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.userNameTextView)
        private val imageView: ImageView = itemView.findViewById(R.id.profileImageView)
        private val lastMessageTextView: TextView = itemView.findViewById(R.id.lastMessageTextView)
        private val fileIconImageView: ImageView = itemView.findViewById(R.id.fileIconImageView)
        private val unreadCountTextView: TextView = itemView.findViewById(R.id.unreadCountTextView)

        /**
         * Binds a user or car item to the ViewHolder.
         * Sets up the display of the name, image, and last message, including file icons if necessary.
         *
         * @param item The item to bind, which can be a UserWithLastMessage or CarWithLastMessage.
         */
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
                    if (item.unreadCount > 0) {
                        unreadCountTextView.visibility = View.VISIBLE
                        unreadCountTextView.text = item.unreadCount.toString()
                    } else {
                        unreadCountTextView.visibility = View.GONE
                    }
                }
                is CarWithLastMessage -> {
                    nameTextView.text = "${item.car.modelo} - ${item.owner.firstName}"
                    setFileIconAndMessage(item.fileType, item.isFile, item.lastMessage, item.fileName)
                    val imageUrl = item.car.imageUrls?.firstOrNull()
                    Glide.with(itemView.context)
                        .load(imageUrl)
                        .placeholder(R.drawable.ic_image)
                        .error(R.drawable.ic_error)
                        .into(imageView)
                    if (item.unreadCount > 0) {
                        unreadCountTextView.visibility = View.VISIBLE
                        unreadCountTextView.text = item.unreadCount.toString()
                    } else {
                        unreadCountTextView.visibility = View.GONE
                    }
                }
            }
            itemView.setOnClickListener { onItemClicked(item) }
        }

        /**
         * Configures the file icon and message text based on the file type and presence.
         *
         * @param fileType The MIME type of the file, if any.
         * @param isFile Indicates if the message contains a file.
         * @param messageText The last message text to display.
         * @param fileName The name of the file, if applicable.
         */
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
     * Determines the view type based on the position in the list.
     * Displays headers for recent and other sections or user/car items.
     */
    override fun getItemViewType(position: Int): Int {
        return when (position) {
            0 -> VIEW_TYPE_RECENT_HEADER
            recentItems.size + 1 -> VIEW_TYPE_OTHER_HEADER
            else -> VIEW_TYPE_ITEM
        }
    }

    /**
     * Creates a new ViewHolder based on the view type.
     */
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

    /**
     * Binds data to the ViewHolder based on position.
     * Sets up section headers or user/car items as needed.
     */
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

    /**
     * Returns the total number of items to be displayed in the RecyclerView.
     * Includes both recent and other sections, along with their headers.
     */
    override fun getItemCount(): Int = recentItems.size + otherItems.size + 2

    /**
     * Updates the data in the adapter for both recent and other sections.
     * Notifies the adapter to refresh the view.
     *
     * @param newRecentItems The updated list of recent items.
     * @param newOtherItems The updated list of other items.
     */
    fun updateData(newRecentItems: List<Any>, newOtherItems: List<Any>) {
        // Filtra elementos con mensajes válidos
        recentItems.clear()
        recentItems.addAll(newRecentItems.filter {
            (it is UserWithLastMessage && it.lastMessage.isNotEmpty()) ||
                    (it is CarWithLastMessage && it.lastMessage.isNotEmpty())
        })

        otherItems.clear()
        otherItems.addAll(newOtherItems.filter {
            (it is UserWithLastMessage && it.lastMessage.isNotEmpty()) ||
                    (it is CarWithLastMessage && it.lastMessage.isNotEmpty())
        })

        Log.d("UsersMessagesAdapter", "recentItems: $recentItems")
        Log.d("UsersMessagesAdapter", "otherItems: $otherItems")

        notifyDataSetChanged()
    }


    /**
     * ViewHolder for displaying section headers ("Recent Chats" and "Other Chats").
     */
    inner class SectionHeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val headerTextView: TextView = itemView.findViewById(R.id.sectionHeaderTextView)

        /**
         * Binds the header text to the section header.
         *
         * @param headerText The header text to display.
         */
        fun bind(headerText: String) {
            headerTextView.text = headerText
        }
    }
}
