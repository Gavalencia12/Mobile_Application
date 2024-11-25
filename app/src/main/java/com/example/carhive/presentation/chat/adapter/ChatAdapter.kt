package com.example.carhive.presentation.chat.adapter

import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.carhive.Domain.model.Message
import com.example.carhive.R
import com.example.carhive.data.model.DownloadedFileEntity
import com.example.carhive.di.DatabaseModule
import com.example.carhive.presentation.chat.dialog.MediaViewerDialogFragment
import com.example.carhive.utils.saveFileToMediaStorage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Adapter class for displaying chat messages in a RecyclerView.
 * Handles different message types (text, images, videos, files) for sent and received messages.
 *
 * @param messages List of messages to display.
 * @param fragmentManager FragmentManager for displaying media viewer dialogs.
 * @param coroutineScope Coroutine scope for handling asynchronous operations.
 */
class ChatAdapter(
    private val messages: MutableList<Message>,
    private val fragmentManager: FragmentManager,
    private val coroutineScope: CoroutineScope,
    private val admin: Boolean
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val VIEW_TYPE_SENT = 1
        const val VIEW_TYPE_RECEIVED = 2
    }

    /**
     * Determines the view type (sent or received) based on the message sender.
     */
    override fun getItemViewType(position: Int): Int {
        val currentMessage = messages[position]
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        return if (currentMessage.senderId == currentUserId || admin && currentMessage.senderId == "TechnicalSupport") VIEW_TYPE_SENT else VIEW_TYPE_RECEIVED
    }

    /**
     * Inflates the appropriate layout based on the view type.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MessageViewHolder(
            LayoutInflater.from(parent.context).inflate(
                if (viewType == VIEW_TYPE_SENT) R.layout.item_message_sent else R.layout.item_message_received,
                parent,
                false
            )
        )
    }

    /**
     * Binds the message data to the ViewHolder and updates the status icon.
     */
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]
        val previousMessage = if (position > 0) messages[position - 1] else null
        if (holder is MessageViewHolder) {
            holder.bind(message, previousMessage)
            holder.updateStatusIcon(message.status)
        }
    }

    override fun getItemCount(): Int = messages.size

    /**
     * Updates the list of messages and notifies the adapter of data changes.
     */
    fun updateMessages(newMessages: List<Message>) {
        messages.clear()
        messages.addAll(newMessages)
        notifyDataSetChanged()
    }

    /**
     * ViewHolder class for displaying individual messages.
     */
    inner class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageTextView: TextView = itemView.findViewById(R.id.messageText)
        private val timeTextView: TextView = itemView.findViewById(R.id.messageTime)
        private val fileContainer: LinearLayout = itemView.findViewById(R.id.fileContainer)
        private val fileNameTextView: TextView = itemView.findViewById(R.id.fileName)
        private val fileInfoTextView: TextView = itemView.findViewById(R.id.fileInfo)
        private val retryButton: Button = itemView.findViewById(R.id.retryButton)
        private val dateTextView: TextView = itemView.findViewById(R.id.dateHeader)
        private val statusIcon: ImageView = itemView.findViewById(R.id.statusIcon)
        private val fileImageView: ImageView = itemView.findViewById(R.id.fileImageView)

        /**
         * Binds message data to the view components and configures the message type (text, image, etc.).
         */
        fun bind(message: Message, previousMessage: Message?) {

            messageTextView.visibility = View.GONE
            fileContainer.visibility = View.GONE
            fileImageView.visibility = View.GONE
            retryButton.visibility = View.GONE
            fileImageView.setImageDrawable(null)

            // Shows the date header if this is the first message of the day
            if (previousMessage == null || message.getFormattedDate() != previousMessage.getFormattedDate()) {
                dateTextView.visibility = View.VISIBLE
                dateTextView.text = message.getFormattedDate()
            } else {
                dateTextView.visibility = View.GONE
            }

            when {
                message.fileType?.startsWith("image/") == true -> configureImageView(message)
                message.fileType?.startsWith("video/") == true -> configureVideoView(message)
                message.fileType != null -> configureFileContainer(message)
                message.content != null -> {
                    messageTextView.visibility = View.VISIBLE
                    messageTextView.text = message.content
                }
            }
            updateStatusIcon(message.status)

            timeTextView.text = message.getFormattedTime()
        }

        /**
         * Configures the layout for a file message and sets up download handling.
         */
        private fun configureFileContainer(message: Message) {
            fileContainer.visibility = View.VISIBLE
            fileNameTextView.text = message.fileName
            fileInfoTextView.text = "${formatFileSize(message.fileSize)} • ${getFriendlyFileType(message.fileType ?: "")}"

            val context = itemView.context
            downloadAndSaveFile(context, message) { fileUri ->
                if (fileUri != null) {
                    fileContainer.setOnClickListener {
                        coroutineScope.launch {
                            openFileWithApp(context, message.hash ?: "", message.fileType ?: "*/*")
                        }
                    }
                } else {
                    Toast.makeText(context, "File not found or downloading.", Toast.LENGTH_SHORT).show()
                }
            }

            retryButton.setOnClickListener {
                downloadAndSaveFile(context, message) { }
            }
        }

        /**
         * Configures the layout for displaying an image message with Glide.
         */
        private fun configureImageView(message: Message) {
            fileImageView.visibility = View.VISIBLE
            val context = itemView.context
            downloadAndSaveFile(context, message) { fileUri ->
                fileUri?.let {
                    Glide.with(context)
                        .load(it)
                        .placeholder(R.drawable.ic_image)
                        .error(R.drawable.ic_error)
                        .into(fileImageView)
                    fileImageView.setOnClickListener {
                        openMediaModal(messages.indexOf(message))
                    }
                }
            }
        }

        /**
         * Configures the layout for displaying a video message with Glide.
         */
        private fun configureVideoView(message: Message) {
            fileImageView.visibility = View.VISIBLE
            val context = itemView.context
            downloadAndSaveFile(context, message) { fileUri ->
                fileUri?.let {
                    Glide.with(context)
                        .load(it)
                        .placeholder(R.drawable.ic_videos)
                        .error(R.drawable.ic_error)
                        .into(fileImageView)
                    fileImageView.setOnClickListener {
                        openMediaModal(messages.indexOf(message))
                    }
                }
            }
        }

        /**
         * Downloads a file if not already downloaded and saves it locally.
         */
        private fun downloadAndSaveFile(context: Context, message: Message, onFileReady: (Uri?) -> Unit) {
            val fileUrl = message.fileUrl
            val fileHash = message.hash ?: return
            val fileName = message.fileName ?: "downloaded_file"

            val downloadedFileDao = DatabaseModule.getDatabase(context).downloadedFileDao()

            coroutineScope.launch {
                val existingFile = downloadedFileDao.getFileByHash(fileHash)

                if (existingFile != null) {
                    onFileReady(Uri.parse(existingFile.filePath))
                    return@launch
                }

                retryButton.visibility = View.GONE

                if (fileUrl != null) {
                    val storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(fileUrl)
                    storageReference.getBytes(Long.MAX_VALUE).addOnSuccessListener { bytes ->
                        val fileUri = message.fileType?.let {
                            saveFileToMediaStorage(context, bytes, it, fileHash, fileName)
                        }
                        if (fileUri != null) {
                            val filePath = fileUri.toString()
                            val downloadedFile = DownloadedFileEntity(fileHash, fileName, filePath, message.fileType ?: "application/octet-stream")
                            coroutineScope.launch { downloadedFileDao.insertFile(downloadedFile) }
                            onFileReady(fileUri)
                        } else {
                            showRetryButton()
                            onFileReady(null)
                        }
                    }.addOnFailureListener {
                        showRetryButton()
                        onFileReady(null)
                    }
                } else {
                    showRetryButton()
                    onFileReady(null)
                }
            }
        }

        /**
         * Opens the downloaded file in an appropriate app.
         */
        private suspend fun openFileWithApp(context: Context, fileHash: String, mimeType: String) {
            val fileDao = DatabaseModule.getDatabase(context).downloadedFileDao()
            val file = fileDao.getFileByHash(fileHash) ?: run {
                Toast.makeText(context, "File not found in database.", Toast.LENGTH_SHORT).show()
                return
            }

            val uriBase: Uri = MediaStore.Files.getContentUri("external")
            var fileUri: Uri? = null

            val projection = arrayOf(MediaStore.MediaColumns._ID)
            val selection = "${MediaStore.MediaColumns.DISPLAY_NAME} = ?"
            val selectionArgs = arrayOf(file.fileName)

            context.contentResolver.query(uriBase, projection, selection, selectionArgs, null).use { cursor ->
                if (cursor != null && cursor.moveToFirst()) {
                    val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID))
                    fileUri = ContentUris.withAppendedId(uriBase, id)
                }
            }

            fileUri?.let {
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(it, mimeType)
                    flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                }

                val activities = context.packageManager.queryIntentActivities(intent, 0)
                if (activities.isNotEmpty()) {
                    context.startActivity(intent)
                } else {
                    val fallbackIntent = Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(it, "application/*")
                        flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                    }
                    context.startActivity(fallbackIntent)
                }
            } ?: run {
                Toast.makeText(context, "File not available in storage.", Toast.LENGTH_SHORT).show()
            }
        }

        /**
         * Provides a user-friendly file type name based on MIME type.
         */
        private fun getFriendlyFileType(mimeType: String): String {
            return when {
                mimeType.startsWith("application/pdf") -> "pdf"
                mimeType.startsWith("application/vnd.openxmlformats-officedocument.wordprocessingml.document") -> "docx"
                mimeType.startsWith("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet") -> "xlsx"
                mimeType.startsWith("text/plain") -> "txt"
                mimeType.startsWith("text/x-java") -> "java"
                else -> "file"
            }
        }

        /**
         * Formats the file size in a readable format.
         */
        private fun formatFileSize(fileSizeInBytes: Long): String {
            val kiloBytes = fileSizeInBytes / 1024
            val megaBytes = kiloBytes / 1024
            return when {
                megaBytes > 0 -> "$megaBytes MB"
                kiloBytes > 0 -> "$kiloBytes KB"
                else -> "$fileSizeInBytes B"
            }
        }

        /**
         * Shows the retry button and prompts the user in case of download failure.
         */
        private fun showRetryButton() {
            retryButton.visibility = View.VISIBLE
            Toast.makeText(itemView.context, "Error downloading the file. Please try again.", Toast.LENGTH_SHORT).show()
        }

        /**
         * Updates the status icon based on the message status.
         */
        fun updateStatusIcon(status: String) {
            when (status) {
                "sent" -> statusIcon.setImageResource(R.drawable.ic_negative_message)
                "read" -> statusIcon.setImageResource(R.drawable.ic_check_chat)
                "failed" -> statusIcon.setImageResource(R.drawable.ic_error_internet)
            }
        }

        /**
         * Opens a media viewer dialog for the selected message.
         */
        private fun openMediaModal(position: Int) {
            val mediaViewerDialog = MediaViewerDialogFragment.newInstance(messages, position)
            mediaViewerDialog.show(fragmentManager, "MediaViewerDialog")
        }
    }
}
